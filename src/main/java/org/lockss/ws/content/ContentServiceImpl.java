/*

 Copyright (c) 2014-2020 Board of Trustees of Leland Stanford Jr. University,
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
package org.lockss.ws.content;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lockss.laaws.rs.model.Artifact;
import org.lockss.laaws.rs.model.ArtifactPageInfo;
import org.lockss.log.L4JLogger;
import org.lockss.ws.BaseServiceImpl;
import org.lockss.ws.entities.FileWsResult;
import org.lockss.ws.entities.LockssWebServicesFault;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * The Content SOAP web service implementation.
 */
@Service
public class ContentServiceImpl extends BaseServiceImpl
implements ContentService {
  final static String allVersions = "all";
  final static String anyVersion = "latest";
  private final static L4JLogger log = L4JLogger.getLogger();

  /**
   * Provides a list of the versions of a URL in an Archival Unit.
   * 
   * @param url  A String with the URL.
   * @param auId A String with the identifier (auid) of the archival unit.
   * @return a {@code List<FileWsResult>} with the results.
   * @throws LockssWebServicesFault if there are problems.
   */
  public List<FileWsResult> getVersions(String url, String auId)
      throws LockssWebServicesFault {
    log.debug2("url = {}", url);
    log.debug2("auId = {}", auId);

    try {
      List<FileWsResult> result = new ArrayList<>();

      // Loop through all the artifacts in the response from the REST service.
      for (Artifact artifact : getArtifacts(auId, url, allVersions)) {
	// Create the result data for this artifact.
	FileWsResult fileWsResult = new FileWsResult();
	fileWsResult.setUrl(artifact.getUri());
	fileWsResult.setVersion(artifact.getVersion());
	fileWsResult.setSize(artifact.getContentLength());
	fileWsResult.setCollectionDate(artifact.getCollectionDate());

	// Add this artifact data to the results.
	result.add(fileWsResult);
      }

      log.debug2("result = {}", result);
      return result;
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }

  /**
   * Provides an indication of whether the content defined by a URL and Archival
   * Unit is cached.
   * 
   * @param url  A String with the URL.
   * @param auId A String with the identifier (auid) of the archival unit.
   * @return a boolean with the indication.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public boolean isUrlCached(String url, String auId)
      throws LockssWebServicesFault {
    return isUrlVersionCached(url, auId, null);
  }

  /**
   * Provides an indication of whether the content defined by a URL, an Archival
   * Unit and a version is cached.
   * 
   * @param url     A String with the URL.
   * @param auId    A String with the identifier (auid) of the archival unit.
   * @param version An Integer with the requested version of the content.
   * @return a boolean with the indication.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public boolean isUrlVersionCached(String url, String auId, Integer version)
      throws LockssWebServicesFault {
    log.debug2("url = {}", url);
    log.debug2("auId = {}", auId);
    log.debug2("version = {}", version);

    String versionAsString = version == null ? anyVersion : version.toString();
    log.debug2("versionAsString = {}", versionAsString);

    try {
      boolean result = !getArtifacts(auId, url, versionAsString).isEmpty();
      log.debug2("result = {}", result);
      return result;
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }

  /**
   * Provides the artifacts for a URL in an Archival Unit with a given version,
   * if specified.
   * 
   * @param auId    A String with the identifier (auid) of the archival unit.
   * @param url     A String with the URL.
   * @param version A String with the artifact version required.
   * @return a {@code List<FileWsResult>} with the results.
   * @throws LockssWebServicesFault if there are problems.
   */
  private List<Artifact> getArtifacts(String auId, String url, String version)
      throws Exception {
    log.debug2("auId = {}", auId);
    log.debug2("url = {}", url);
    log.debug2("version = {}", version);

    if (auId == null || auId.isEmpty()) {
      throw new IllegalArgumentException("Missing required Archival Unit "
	  + "identifier (auId)");
    }

    if (url == null || url.isEmpty()) {
      throw new IllegalArgumentException("Missing required URL");
    }

    if (version == null || version.isEmpty()) {
      throw new IllegalArgumentException("Missing required version");
    }

    // Prepare the endpoint URI.
    String endpointUri = env.getProperty(REPO_SVC_URL_KEY) + "/collections/"
	+ env.getProperty(REPO_COLLECTION_KEY) + "/aus/{auId}/artifacts";
    log.trace("endpointUri = {}", endpointUri);

    // Prepare the URI path variables.
    Map<String, String> uriVariables = new HashMap<>(1);
    uriVariables.put("auId", auId);
    log.trace("uriVariables = {}", uriVariables);

    // Prepare the query parameters.
    Map<String, String> queryParams = new HashMap<>(1);
    queryParams.put("url", url);
    queryParams.put("version", version);
    log.trace("queryParams = {}", queryParams);

    List<Artifact> result = new ArrayList<>();
    boolean done = false;

    // Loop while there are more results to be returned by the REST service.
    while (!done) {
      // Make the REST call.
      ResponseEntity<String> response = callRestServiceUri(endpointUri,
	  uriVariables, queryParams, HttpMethod.GET, (Void)null,
	  "Can't get artifact versions");

      // Get the response body.
      ObjectMapper mapper = new ObjectMapper();
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
	       false);
      ArtifactPageInfo api =
	  mapper.readValue(response.getBody(), ArtifactPageInfo.class);

      // Add to the results the artifacts in the response.
      result.addAll(api.getArtifacts());

      // Get the endpoint URI for the next page of results. 
      endpointUri = api.getPageInfo().getNextLink();
      log.trace("endpointUri = {}", endpointUri);

      // The endpoint URI for the next page of results is explicit, not
      // requiring interpolation.
      uriVariables = null;
      queryParams = null;

      // Determine whether the REST service has provided all the results.
      done = endpointUri == null;
    }

    log.debug2("result = {}", result);
    return result;
  }
}
