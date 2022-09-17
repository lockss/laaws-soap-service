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

import org.apache.cxf.jaxrs.ext.multipart.InputStreamDataSource;
import org.lockss.laaws.rs.core.LockssRepository;
import org.lockss.laaws.rs.model.Artifact;
import org.lockss.laaws.rs.model.ArtifactData;
import org.lockss.log.L4JLogger;
import org.lockss.ws.BaseServiceImpl;
import org.lockss.ws.entities.ContentResult;
import org.lockss.ws.entities.FileWsResult;
import org.lockss.ws.entities.LockssWebServicesFault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import javax.activation.DataHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/** The Content SOAP web service implementation. */
@Service
public class ContentServiceImpl extends BaseServiceImpl implements ContentService {
  private static final L4JLogger log = L4JLogger.getLogger();

  /**
   * Provides a list of the versions of a URL in an Archival Unit.
   *
   * @param url A String with the URL.
   * @param auId A String with the identifier (auid) of the archival unit.
   * @return a {@code List<FileWsResult>} with the results.
   * @throws LockssWebServicesFault if there are problems.
   */
  public List<FileWsResult> getVersions(String url, String auId) throws LockssWebServicesFault {
    log.debug2("url = {}", url);
    log.debug2("auId = {}", auId);

    try {
      List<FileWsResult> result = new ArrayList<>();

      // Loop through all the artifacts in the response from the REST service.
      for (Artifact artifact :
          getRestLockssRepository()
              .getArtifactsAllVersions(repoCollection, auId, url)) {
        log.trace("artifact = {}", artifact);

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
   * Provides an indication of whether the content defined by a URL and Archival Unit is cached.
   *
   * @param url A String with the URL.
   * @param auId A String with the identifier (auid) of the archival unit.
   * @return a boolean with the indication.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public boolean isUrlCached(String url, String auId) throws LockssWebServicesFault {
    log.debug2("url = {}", url);
    log.debug2("auId = {}", auId);

    try {
      boolean result =
        getRestLockssRepository().getArtifact(repoCollection, auId, url)
              != null;
      log.debug2("result = {}", result);
      return result;
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }

  /**
   * Provides an indication of whether the content defined by a URL, an Archival Unit and a version
   * is cached.
   *
   * @param url A String with the URL.
   * @param auId A String with the identifier (auid) of the archival unit.
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

    try {
      boolean result =
          getRestLockssRepository()
                  .getArtifactVersion(
                      repoCollection, auId, url, version, false)
              != null;
      log.debug2("result = {}", result);
      return result;
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }

  /**
   * Provides the content defined by a URL and Archival Unit.
   *
   * @param url A String with the URL.
   * @param auId A String with the identifier (auid) of the archival unit.
   * @return a ContentResult with the result of the operation.
   * @throws LockssWebServicesFault if there are problems.
   */
  public ContentResult fetchFile(String url, String auId) throws LockssWebServicesFault {
    log.debug2("url = {}", url);
    log.debug2("auId = {}", auId);

    try {
      if (auId == null || auId.isEmpty()) {
        throw new IllegalArgumentException("Missing required Archival Unit " + "identifier (auId)");
      }

      if (url == null || url.isEmpty()) {
        throw new IllegalArgumentException("Missing required URL");
      }

      ContentResult result = new ContentResult();

      Artifact artifact =
          getRestLockssRepository().getArtifact(repoCollection, auId, url);
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
   * @param url A String with the URL.
   * @param auId A String with the identifier (auid) of the archival unit.
   * @param version An Integer with the requested version of the content.
   * @return a ContentResult with the result of the operation.
   * @throws LockssWebServicesFault if there are problems.
   */
  public ContentResult fetchVersionedFile(String url, String auId, Integer version)
      throws LockssWebServicesFault {
    log.debug2("url = {}", url);
    log.debug2("auId = {}", auId);
    log.debug2("version = {}", version);

    try {
      if (auId == null || auId.isEmpty()) {
        throw new IllegalArgumentException("Missing required Archival Unit " + "identifier (auId)");
      }

      if (url == null || url.isEmpty()) {
        throw new IllegalArgumentException("Missing required URL");
      }

      if (version == null) {
        throw new IllegalArgumentException("Missing required version");
      }

      ContentResult result = new ContentResult();

      Artifact artifact =
          getRestLockssRepository()
              .getArtifactVersion(repoCollection, auId, url, version, false);
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
  private ContentResult getContentResultFromArtifact(Artifact artifact) throws Exception {
    ContentResult result = new ContentResult();

    ArtifactData artifactData =
        getRestLockssRepository().getArtifactData(artifact, LockssRepository.IncludeContent.ALWAYS);
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

      result.setDataHandler(
          new DataHandler(
              new InputStreamDataSource(
                  artifactData.getInputStream(), contentType, artifact.getUri())));

      Properties props = new Properties();

      for (String key : artifactData.getMetadata().keySet()) {
        // TODO: Replace with StringUtil method once StringUtil has been
        // moved from the lockss-core project to the lockss-util project.
        String value =
            separatedString(artifactData.getMetadata().get(key), "", ",", "", new StringBuilder())
                .toString();
        props.setProperty(key, value);
      }

      result.setProperties(props);
    }

    log.debug2("result = {}", result);
    return result;
  }
}
