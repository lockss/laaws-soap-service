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
package org.lockss.ws.export;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.activation.DataHandler;
import javax.xml.ws.soap.MTOM;
import org.apache.cxf.attachment.AttachmentDataSource;
import org.lockss.log.L4JLogger;
import org.lockss.util.rest.RestUtil;
import org.lockss.util.rest.multipart.MultipartConnector;
import org.lockss.util.rest.multipart.MultipartResponse;
import org.lockss.util.rest.multipart.MultipartResponse.Part;
import org.lockss.ws.BaseServiceImpl;
import org.lockss.ws.entities.DataHandlerWrapper;
import org.lockss.ws.entities.ExportServiceParams;
import org.lockss.ws.entities.ExportServiceWsResult;
import org.lockss.ws.entities.LockssWebServicesFault;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

/**
 * The Export SOAP web service implementation.
 */
@MTOM
@Service
public class ExportServiceImpl extends BaseServiceImpl
implements ExportService {
  private final static L4JLogger log = L4JLogger.getLogger();

  @Autowired
  private Environment env;

  /**
   * Exports an Archival Unit.
   * 
   * @param exportParam An ExportServiceParams with the parameters of the export
   *                    operation.
   * @return a ExportServiceWsResult with the result of the export operation.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public ExportServiceWsResult createExportFiles(ExportServiceParams
      exportParam) throws LockssWebServicesFault {
    log.debug2("exportParam = {}", exportParam);

    try {
      // Prepare the endpoint URI.
      String endpointUri =
	  env.getProperty(POLLER_SVC_URL_KEY) + "/aus/{auId}/export";
      log.trace("endpointUri = {}", endpointUri);

      // Prepare the URI path variables.
      Map<String, String> uriVariables = new HashMap<>(1);
      uriVariables.put("auId", exportParam.getAuid());
      log.trace("uriVariables = {}", uriVariables);

      // Prepare the query parameters.
      Map<String, String> queryParams = new HashMap<>(1);
      queryParams.put("fileType", exportParam.getFileType().toString());
      queryParams.put("isCompress",
	  exportParam.isCompress() ? "true" : "false");
      queryParams.put("isExcludeDirNodes",
	  exportParam.isExcludeDirNodes() ? "true" : "false");
      queryParams.put("xlateFilenames",
	  exportParam.getXlateFilenames().toString());
      queryParams.put("filePrefix", exportParam.getFilePrefix());
      queryParams.put("maxSize",
	  Long.valueOf(exportParam.getMaxSize()).toString());
      queryParams.put("maxVersions",
	  Integer.valueOf(exportParam.getMaxVersions()).toString());
      log.trace("queryParams = {}", queryParams);

      URI uri = RestUtil.getRestUri(endpointUri, uriVariables, queryParams);
      log.trace("uri = {}", uri);

      // Initialize the request headers.
      HttpHeaders requestHeaders = new HttpHeaders();
      requestHeaders.setAccept(Arrays.asList(MediaType.MULTIPART_FORM_DATA,
  	MediaType.APPLICATION_JSON));

      // Get any incoming authorization header with credentials to be passed to
      // the REST service.
      String authHeaderValue = getSoapRequestAuthorizationHeader();
      log.trace("authHeaderValue = {}", authHeaderValue);

      // Check whether there are credentials to be sent.
      if (authHeaderValue != null && !authHeaderValue.isEmpty()) {
        // Yes: Add them to the request.
        requestHeaders.set("Authorization", authHeaderValue);
      }

      log.trace("requestHeaders = {}", requestHeaders);

      // Make the request and obtain the response.
      MultipartResponse response = new MultipartConnector(uri, requestHeaders)
  	.requestGet(getConnectionTimeout().intValue(),
  	    getReadTimeout().intValue());

      HttpStatus statusCode = response.getStatusCode();
      log.trace("statusCode = " + statusCode);

      if (statusCode.equals(HttpStatus.OK)) {
	Map<String, Part> parts = response.getParts();
	log.trace("parts = " + parts);

	int partCount = parts.size();
	log.trace("partCount = " + partCount);

	DataHandlerWrapper[] dataHandlerWrapperArray =
	    new DataHandlerWrapper[partCount];

	int partIndex = 0;

	for (String name : parts.keySet()) {
	  log.trace("name = " + name);

	  Part part = parts.get(name);
	  log.trace("part = " + part);

	  String contentType = part.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
	  log.trace("contentType = " + contentType);

	  AttachmentDataSource source =
	      new AttachmentDataSource(contentType, part.getInputStream());
	  log.trace("source = " + source);

	  source.setName(name);

	  DataHandler dataHandler = new DataHandler(source);
	
	  long size = part.getContentLength();
	  log.trace("size = " + size);

	  DataHandlerWrapper dataHandlerWrapper = new DataHandlerWrapper();
	  dataHandlerWrapper.setDataHandler(dataHandler);
	  dataHandlerWrapper.setSize(size);
	  dataHandlerWrapper.setName(source.getName());
	  dataHandlerWrapperArray[partIndex++] = dataHandlerWrapper;
	}

	ExportServiceWsResult result = new ExportServiceWsResult();
	result.setDataHandlerWrappers(dataHandlerWrapperArray);
	result.setAuId(exportParam.getAuid());

	log.debug2("result = {}", result);
	return result;
      } else {
	String message = "REST service returned statusCode '" + statusCode
	    + ", statusMessage = '" + response.getStatusMessage() + "'";

	log.error(message);
	throw new RuntimeException(message);
      }
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }
}
