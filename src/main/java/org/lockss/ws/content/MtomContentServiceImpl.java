/*

Copyright (c) 2000-2020 Board of Trustees of Leland Stanford Jr. University,
all rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice,
this list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

3. Neither the name of the copyright holder nor the names of its contributors
may be used to endorse or promote products derived from this software without
specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 */
package org.lockss.ws.content;

import java.util.Properties;
import javax.activation.DataHandler;
import javax.xml.ws.soap.MTOM;
import org.apache.cxf.jaxrs.ext.multipart.InputStreamDataSource;
import org.lockss.laaws.rs.model.Artifact;
import org.lockss.laaws.rs.model.ArtifactData;
import org.lockss.log.L4JLogger;
import org.lockss.ws.BaseServiceImpl;
import org.lockss.ws.entities.ContentResult;
import org.lockss.ws.entities.LockssWebServicesFault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

/**
 * The Message Transmission Optimization Mechanism (MTOM) Content SOAP web
 * service implementation.
 */
@MTOM
@Service
public class MtomContentServiceImpl extends BaseServiceImpl
implements MtomContentService {
  final static String anyVersion = "latest";
  private final static L4JLogger log = L4JLogger.getLogger();

  /**
   * Provides the content defined by a URL and Archival Unit.
   * 
   * @param url  A String with the URL.
   * @param auId A String with the identifier (auid) of the archival unit.
   * @return a ContentResult with the result of the operation.
   * @throws LockssWebServicesFault if there are problems.
   */
  public ContentResult fetchFile(String url, String auId)
      throws LockssWebServicesFault {
    log.debug2("url = {}", url);
    log.debug2("auId = {}", auId);

    try {
      if (auId == null || auId.isEmpty()) {
        throw new IllegalArgumentException("Missing required Archival Unit "
  	  + "identifier (auId)");
      }

      if (url == null || url.isEmpty()) {
        throw new IllegalArgumentException("Missing required URL");
      }

      ContentResult result = new ContentResult();

      Artifact artifact = getRestLockssRepository()
	  .getArtifact(env.getProperty(REPO_COLLECTION_KEY), auId, url);
      log.trace("artifact = {}", artifact);

      if (artifact != null) {
	result = getContentResultFromArtifact(artifact);
      }

      log.debug2("result = {}", result);
      return result;
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }

  /**
   * Provides the content defined by a URL, an Archival Unit and a version.
   * 
   * @param url     A String with the URL.
   * @param auId    A String with the identifier (auid) of the archival unit.
   * @param version An Integer with the requested version of the content.
   * @return a ContentResult with the result of the operation.
   * @throws LockssWebServicesFault if there are problems.
   */
  public ContentResult fetchVersionedFile(String url, String auId,
      Integer version) throws LockssWebServicesFault {
    log.debug2("url = {}", url);
    log.debug2("auId = {}", auId);
    log.debug2("version = {}", version);

    try {
      if (auId == null || auId.isEmpty()) {
	throw new IllegalArgumentException("Missing required Archival Unit "
	    + "identifier (auId)");
      }

      if (url == null || url.isEmpty()) {
	throw new IllegalArgumentException("Missing required URL");
      }

      if (version == null) {
	throw new IllegalArgumentException("Missing required version");
      }

      ContentResult result = new ContentResult();

      Artifact artifact = getRestLockssRepository().getArtifactVersion(
	  env.getProperty(REPO_COLLECTION_KEY), auId, url, version, false);
      log.trace("artifact = {}", artifact);

      if (artifact != null) {
	result = getContentResultFromArtifact(artifact);
      }

      log.debug2("result = {}", result);
      return result;
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }

  /**
   * Provides the SOAP service operation result for a given Artifact.
   * 
   * @param artifact the Artifact that is the data source.
   * @return a ContentResult with the SOAP service operation result.
   * @throws Exception if there are problems.
   */
  private ContentResult getContentResultFromArtifact(Artifact artifact)
      throws Exception {
    ContentResult result = new ContentResult();

    ArtifactData artifactData =
	getRestLockssRepository().getArtifactData(artifact, true);
    log.trace("artifactData = {}", artifactData);

    if (artifactData != null) {
      String contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;

	    HttpHeaders metadata = artifactData.getMetadata();
      log.trace("metadata = {}", metadata);

      if (metadata != null) {
	MediaType mediaType = metadata.getContentType();
	log.trace("mediaType = {}", mediaType);

	if (mediaType != null) {
	  contentType = mediaType.toString();
	  log.trace("contentType = {}", contentType);
	}
      }

      result.setDataHandler(new DataHandler(new InputStreamDataSource(
	  artifactData.getInputStream(), contentType, artifact.getUri())));

      Properties props = new Properties();

      for (String key : artifactData.getMetadata().keySet()) {
	// TODO: Replace with StringUtil method once StringUtil has been
	// moved from the lockss-core project to the lockss-util project.
	String value = separatedString(artifactData.getMetadata().get(key),
	    "", ",", "", new StringBuilder()).toString();
	props.setProperty(key, value);
      }

      result.setProperties(props);
    }

    log.debug2("result = {}", result);
    return result;
  }

//  /**
//   * Provides the content defined by a URL, an Archival Unit and a version.
//   * 
//   * @param url     A String with the URL.
//   * @param auId    A String with the identifier (auid) of the archival unit.
//   * @param version A String with the file version required.
//   * @return a ContentResult with the result of the operation.
//   * @throws LockssWebServicesFault if there are problems.
//   */
//  private ContentResult getArtifact(String url, String auId, String version)
//      throws Exception {
//    log.debug2("url = {}", url);
//    log.debug2("auId = {}", auId);
//    log.debug2("version = {}", version);
//
//    if (auId == null || auId.isEmpty()) {
//      throw new IllegalArgumentException("Missing required Archival Unit "
//	  + "identifier (auId)");
//    }
//
//    if (url == null || url.isEmpty()) {
//      throw new IllegalArgumentException("Missing required URL");
//    }
//
//    if (version == null || version.isEmpty()) {
//      throw new IllegalArgumentException("Missing required version");
//    }
//
//    // Prepare the endpoint URI.
//    String endpointUri = env.getProperty(REPO_SVC_URL_KEY)
//	+ "/collections/{collection}/aus/{auId}/artifacts";
//    log.trace("endpointUri = {}", endpointUri);
//
//    // Prepare the URI path variables.
//    Map<String, String> uriVariables = new HashMap<>(1);
//    uriVariables.put("collection", env.getProperty(REPO_COLLECTION_KEY));
//    uriVariables.put("auId", auId);
//    log.trace("uriVariables = {}", uriVariables);
//
//    // Prepare the query parameters.
//    Map<String, String> queryParams = new HashMap<>(1);
//    queryParams.put("url", url);
//    queryParams.put("version", version);
//    log.trace("queryParams = {}", queryParams);
//
//    // Make the REST call.
//    ResponseEntity<String> response = callRestServiceUri(endpointUri,
//	  uriVariables, queryParams, HttpMethod.GET, (Void)null,
//	  "Can't get artifact version");
//
//    // Get the response body.
//    ObjectMapper mapper = new ObjectMapper();
//    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
//	       false);
//    List<Artifact> artifacts = mapper.readValue(response.getBody(),
//	ArtifactPageInfo.class).getArtifacts();
//
//    ContentResult result = new ContentResult();
//
//    if (!artifacts.isEmpty()) {
//      Artifact artifact = artifacts.get(0);
//      log.trace("artifact = {}", artifact);
//
//      String artifactId = artifact.getId();
//      log.trace("artifactId = {}", artifactId);
//
////      ArtifactData artifactData = artCache.getArtifactData(env.getProperty(REPO_COLLECTION_KEY), artifactId,
////	   true);
//
//      // Prepare the endpoint URI.
//      endpointUri = env.getProperty(REPO_SVC_URL_KEY)
//	  + "/collections/{collection}/artifacts/{artifactId}";
//      log.trace("endpointUri = {}", endpointUri);
//
//      uriVariables.put("collection", env.getProperty(REPO_COLLECTION_KEY));
//      uriVariables.put("artifactId", artifactId);
//      log.trace("uriVariables = {}", uriVariables);
//
//      // Initialize the request headers.
//      HttpHeaders requestHeaders = new HttpHeaders();
//      requestHeaders.set("Accept", "multipart/related");
//
//      // Make the REST call.
//      response = callRestServiceUri(endpointUri, uriVariables, null,
//	  HttpMethod.GET, requestHeaders, (Void)null, "Can't get artifact");
//
//      Properties props = new Properties();
//      props.put("someKey", "someValue");
//      result.setProperties(props);
//
//      result.setDataHandler(new DataHandler(new InputStreamDataSource(
//	  new ByteArrayInputStream(response.getBody().getBytes()),
//	  "multipart/related")));
//
////      result.setDataHandler(new DataHandler(new InputStreamDataSource(
////	  artifactData.getInputStream(), "applicationtext/octet-stream")));
//    }
//
//    log.debug2("result = {}", result);
//    return result;
//  }
}
