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

import java.util.ArrayList;
import java.util.List;
import org.lockss.laaws.rs.model.Artifact;
import org.lockss.log.L4JLogger;
import org.lockss.ws.BaseServiceImpl;
import org.lockss.ws.entities.FileWsResult;
import org.lockss.ws.entities.LockssWebServicesFault;
import org.springframework.stereotype.Service;

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
              .getArtifactsAllVersions(env.getProperty(REPO_COLLECTION_KEY), auId, url)) {
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
          getRestLockssRepository().getArtifact(env.getProperty(REPO_COLLECTION_KEY), auId, url)
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
                      env.getProperty(REPO_COLLECTION_KEY), auId, url, version, false)
              != null;
      log.debug2("result = {}", result);
      return result;
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }
}
