/*

Copyright (c) 2015-2020 Board of Trustees of Leland Stanford Jr. University,
all rights reserved.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL
STANFORD UNIVERSITY BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

Except as contained in this notice, the name of Stanford University shall not
be used in advertising or otherwise to promote the sale, use or other dealings
in this Software without prior written authorization from Stanford University.

*/
package org.lockss.ws.importer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.lockss.laaws.rs.util.NamedInputStreamResource;
import org.lockss.log.L4JLogger;
import org.lockss.util.PropertiesUtil;
import org.lockss.util.io.DeferredTempFileOutputStream;
import org.lockss.util.rest.HttpResponseStatusAndHeaders;
import org.lockss.util.rest.RestUtil;
import org.lockss.util.rest.SpringHeaderUtil;
import org.lockss.util.rest.multipart.MultipartConnector;
import org.lockss.ws.BaseServiceImpl;
import org.lockss.ws.entities.ImportWsParams;
import org.lockss.ws.entities.ImportWsResult;
import org.lockss.ws.entities.LockssWebServicesFault;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;

import javax.activation.DataHandler;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** The Import SOAP web service implementation. */
@Service
public class ImportServiceImpl extends BaseServiceImpl implements ImportService {
  private static final L4JLogger log = L4JLogger.getLogger();

  /**
   * The name of the part with the import content.
   *
   * <p>It must be named this way because the use of Swagger 2 requires it. In the Swagger YAML
   * configuration file, a multipart/form-data body payload is represented by the 'in: formData'
   * parameter, which needs to have a type of 'file' and Swagger 2 uses the type as the part name.
   */
  private static final String IMPORT_CONTENT_PART_NAME = "file";

  private static final String BASIC_AUTH_KEY = "BasicAuthorization";

  /**
   * Imports a pulled file into an archival unit.
   *
   * @param importParams An ImportWsParams with the parameters of the importing operation.
   * @return an ImportWsResult with the result of the operation.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public ImportWsResult importPulledFile(ImportWsParams importParams)
      throws LockssWebServicesFault {
    log.debug2("importParams = {}", importParams);

    ImportWsResult wsResult = new ImportWsResult();

    try {
      // Prepare the endpoint URI.
      URI uri = getImportEndpointUri();
      log.trace("uri = {}", uri);

      // Get the user properties.
      String[] userProperties = importParams.getProperties();
      log.trace("userProperties = {}", Arrays.asList(userProperties));

      // Fetch headers and content from source URL
      HttpInputMessage src =
          restTemplate.execute(importParams.getSourceUrl(), HttpMethod.GET, null, response -> {
            try (DeferredTempFileOutputStream dfos =
                     // FIXME: Parameterize DFOS threshold
                     new DeferredTempFileOutputStream((int) FileUtils.ONE_MB, "importSourceUrl")) {

              StreamUtils.copy(response.getBody(), dfos);

              return new HttpInputMessage() {
                @Override
                public HttpHeaders getHeaders() {
                  return response.getHeaders();
                }

                @Override
                public InputStream getBody() throws IOException {
                  return dfos.getDeleteOnCloseInputStream();
                }
              };
            }
          }, PropertiesUtil.convertArrayToMap(userProperties));

      // Perform the REST call to import the content.
      wsResult =
          performRestCall(
              importParams.getTargetId(),
              importParams.getTargetUrl(),
              userProperties,
              src.getBody(),
              uri,
              src.getHeaders().getContentType().toString(),
              src.getHeaders().getContentLength());

    } catch (Exception e) {
      wsResult.setIsSuccess(Boolean.FALSE);
      wsResult.setMessage("Cannot import pushed content: " + e.getMessage());
    }

    log.debug2("wsResult = {}", wsResult);
    return wsResult;
  }

  /**
   * Imports a pushed file into an archival unit.
   *
   * @param importParams An ImportWsParams with the parameters of the importing operation.
   * @return an ImportWsResult with the result of the operation.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public ImportWsResult importPushedFile(ImportWsParams importParams)
      throws LockssWebServicesFault {
    log.debug2("importParams = {}", importParams);

    ImportWsResult wsResult = new ImportWsResult();

    // The stream to the file to be imported.
    InputStream input = null;

    // The temporary file used to find the content length.
    File tmpFile = null;

    try {
      // Prepare the endpoint URI.
      URI uri = getImportEndpointUri();
      log.trace("uri = {}", uri);

      // Get the wrapper of the pushed file to be imported.
      DataHandler dataHandler = importParams.getDataHandler();

      try {
        // Open a stream to the pushed file to be imported.
        input = dataHandler.getInputStream();
      } catch (IOException ioe) {
        wsResult.setIsSuccess(Boolean.FALSE);
        wsResult.setMessage("Cannot open input stream to pushed content: " + ioe.getMessage());

        log.debug2("wsResult = {}", wsResult);
        return wsResult;
      }

      tmpFile = File.createTempFile("imported", "", null);

      try (FileOutputStream fos = new FileOutputStream(tmpFile)) {
        StreamUtils.copy(input, fos);
      } catch (IOException e) {
        log.warn("Error writing to DFOS", e);
      }

      // Clean up.
      if (input != null) {
        try {
          input.close();
        } catch (IOException ioe) {
          log.warn("Exception caught closing input stream", ioe);
        }
      }

      // Continue using the copy just made.
      input = new FileInputStream(tmpFile);

      // Perform the REST call to import the content.
      wsResult =
          performRestCall(
              importParams.getTargetId(),
              importParams.getTargetUrl(),
              importParams.getProperties(),
              input,
              uri,
              // Content-Type will default to application/octet-stream if not
              // specified in the SOAP request:
              dataHandler.getContentType(),
              tmpFile.length());
    } catch (Exception e) {
      wsResult.setIsSuccess(Boolean.FALSE);
      wsResult.setMessage("Cannot import pushed content: " + e.getMessage());
    } finally {
      if (input != null) {
        try {
          input.close();
        } catch (IOException ioe) {
          log.warn("Exception caught closing input stream", ioe);
        }
      }

      if (tmpFile != null) {
        if (!tmpFile.delete()) {
          tmpFile.deleteOnExit();
        }
      }
    }

    log.debug2("wsResult = {}", wsResult);
    return wsResult;
  }

  /**
   * Provides the names of the supported checksum algorithms.
   *
   * @return a String[] with the names of the supported checksum algorithms.
   */
  @Override
  public String[] getSupportedChecksumAlgorithms() throws LockssWebServicesFault {
    log.debug2("Invoked.");

    try {
      // Make the REST call.
      ResponseEntity<String> response =
          callRestServiceEndpoint(
              env.getProperty(REPO_SVC_URL_KEY),
              "/checksumalgorithms",
              null,
              null,
              HttpMethod.GET,
              (Void) null,
              "Can't get supported checksum algorithms");

      // Get the response body.
      try {
        ObjectMapper mapper = new ObjectMapper();
        List<String> list =
            mapper.readValue(response.getBody(), new TypeReference<List<String>>() {});
        log.trace("list = {}", list);

        String[] result = list.toArray(new String[0]);
        log.debug2("result = {}", Arrays.toString(result));
        return result;
      } catch (Exception e) {
        log.error("Cannot get body of response", e);
        throw e;
      }
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }

  /**
   * Provides the endpoint URI for file import operations.
   *
   * @return a URI with the endpoint URI.
   */
  private URI getImportEndpointUri() {
    log.debug2("Invoked");

    // Prepare the endpoint URI.
    String endpointUri = env.getProperty(POLLER_SVC_URL_KEY) + "/aus/import";
    log.trace("endpointUri = {}", endpointUri);

    URI uri = RestUtil.getRestUri(endpointUri, null, null);
    log.debug2("uri = {}", uri);
    return uri;
  }

  /**
   * Provides a connection to the source URL of a pulled file to be imported.
   *
   * @param sourceUrl A String with the URL to the pulled file to be imported.
   * @param properties A {@code Map<String, String>} with the user-specified properties.
   * @return a URLConnection to the URL of the pulled file to be imported.
   * @throws MalformedURLException if the source URL is malformed.
   * @throws IOException if it cannot open a connection or an input stream to the source URL.
   */
  private URLConnection getSourceUrlConnection(String sourceUrl, Map<String, String> properties)
      throws MalformedURLException, IOException {
    log.debug2("sourceUrl = {}", sourceUrl);
    log.debug2("properties = {}", properties);

    URL url = new URL(sourceUrl);
    URLConnection urlConnection = url.openConnection();
    String uInfo = url.getUserInfo();
    log.trace("uInfo = {}", uInfo);

    if (uInfo != null) {
      String basicAuth = "Basic " + DatatypeConverter.printBase64Binary(uInfo.getBytes());
      log.trace("basicAuth = {}", basicAuth);

      urlConnection.setRequestProperty("Authorization", basicAuth);
    } else if (properties.containsKey(BASIC_AUTH_KEY)) {
      String basicAuth = properties.get(BASIC_AUTH_KEY);
      log.trace("basicAuth = {}", basicAuth);

      urlConnection.setRequestProperty("Authorization", basicAuth);
    }

    log.debug2("urlConnection = {}", urlConnection);
    return urlConnection;
  }

  /**
   * Performs the REST call to import content.
   *
   * @param targetId A String with the base URL path of the target AU.
   * @param targetUrl A String with the target AU URL.
   * @param userProperties A {@code String[]} with the user-specified properties.
   * @param input An InputStream with the content to be imported.
   * @param uri A URI with the REST endpoint URI.
   * @param contentType A String with the MIME type of the content to be imported.
   * @param contentLength A long with the length of the content to be imported.
   * @return an ImportWsResult with the result of the call.
   */
  private ImportWsResult performRestCall(
      String targetId,
      String targetUrl,
      String[] userProperties,
      InputStream input,
      URI uri,
      String contentType,
      long contentLength) {
    log.debug2("targetId = {}", targetId);
    log.debug2("targetUrl = {}", targetUrl);
    log.debug2("userProperties = {}", Arrays.asList(userProperties));
    log.debug2("uri = {}", uri);
    log.debug2("contentType = {}", contentType);
    log.debug2("contentLength = {}", contentLength);

    // Initialize the request headers.
    HttpHeaders requestHeaders = new HttpHeaders();
    requestHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    requestHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

    SpringHeaderUtil.addHeaders(getAuthHeaders(), requestHeaders);

    log.trace("requestHeaders = {}", requestHeaders);

    // Add the payload.
    MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();

    parts.add("targetBaseUrlPath", targetId);
    parts.add("targetUrl", targetUrl);
    parts.add("userProperties", userProperties);

    Resource resource = new NamedInputStreamResource(IMPORT_CONTENT_PART_NAME, input);

    // Initialize the part headers.
    HttpHeaders partHeaders = new HttpHeaders();
    partHeaders.setContentType(MediaType.valueOf(contentType));

    // This must be set or else AbstractResource#contentLength will read the
    // entire InputStream to determine the content length, which will exhaust
    // the InputStream.
    partHeaders.setContentLength(contentLength);
    log.trace("partHeaders = {}", partHeaders);

    parts.add(IMPORT_CONTENT_PART_NAME, new HttpEntity<>(resource, partHeaders));
    log.trace("parts = {}", parts);

    // Make the request and obtain the response.
    HttpResponseStatusAndHeaders response =
        new MultipartConnector(uri, requestHeaders, parts)
            .setRestTemplate(restTemplate)
            .requestPut(getConnectionTimeout().intValue(), getReadTimeout().intValue());

    log.trace("response = {}", response);

    // Prepare the result to be returned.
    boolean isSuccess = response.getCode() == HttpStatus.OK.value();
    log.trace("isSuccess = {}", isSuccess);

    ImportWsResult wsResult = new ImportWsResult();
    wsResult.setIsSuccess(isSuccess);

    if (!isSuccess) {
      wsResult.setMessage(response.getMessage());
    }

    log.debug2("wsResult = {}", wsResult);
    return wsResult;
  }
}
