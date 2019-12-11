/*

 Copyright (c) 2014-2019 Board of Trustees of Leland Stanford Jr. University,
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
import org.lockss.log.L4JLogger;
import org.lockss.ws.entities.FileWsResult;
import org.lockss.ws.entities.LockssWebServicesFault;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/**
 * The Content SOAP web service implementation.
 */
@Service
public class ContentServiceImpl implements ContentService {
  private final static L4JLogger log = L4JLogger.getLogger();

  @Autowired
  private Environment env;

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
    log.debug2("url = {}, auId = {}", url, auId);

    if (url == null || url.isEmpty()) {
      throw new LockssWebServicesFault("Missing required URL");
    }

    if (auId == null || auId.isEmpty()) {
      throw new LockssWebServicesFault("Missing required Archival Unit "
	  + "identifier (auId)");
    }

    try {
      // TODO: REPLACE THIS BLOCK WITH THE ACTUAL IMPLEMENTATION.
      List<FileWsResult> result = new ArrayList<FileWsResult>();
      FileWsResult versionedFile = new FileWsResult();

      versionedFile.setUrl(url);
      versionedFile.setVersion(12);
      versionedFile.setSize(345678L);
      versionedFile.setCollectionDate((new java.util.Date()).getTime());

      result.add(versionedFile);
      // TODO: END OF BLOCK TO BE REPLACED.

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
    log.debug2("url = {}, auId = {}", url, auId);

    if (url == null || url.isEmpty()) {
      throw new LockssWebServicesFault("Missing required URL");
    }

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
    log.debug2("url = {}, auId = {}, version = {}", url, auId, version);

    if (url == null || url.isEmpty()) {
      throw new LockssWebServicesFault("Missing required URL");
    }

    if ((auId == null || auId.isEmpty()) && version != null) {
      throw new LockssWebServicesFault("To check a specific version, the "
	  + "Archival Unit identifier (auId) is required");
    }

    try {
      // TODO: REPLACE THIS BLOCK WITH THE ACTUAL IMPLEMENTATION.
      boolean result = true;
      // TODO: END OF BLOCK TO BE REPLACED.

      log.debug2("result = {}", result);
      return result;
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }
}
