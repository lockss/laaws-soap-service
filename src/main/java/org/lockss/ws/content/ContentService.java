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

import org.lockss.ws.entities.ContentResult;
import org.lockss.ws.entities.FileWsResult;
import org.lockss.ws.entities.LockssWebServicesFault;

import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;
import java.util.List;

/** The Content SOAP web service interface. */
@WebService
public interface ContentService {
  /**
   * Provides a list of the versions of a URL in an Archival Unit.
   *
   * @param url A String with the URL.
   * @param auId A String with the identifier (auid) of the archival unit.
   * @return a {@code List<FileWsResult>} with the results.
   * @throws LockssWebServicesFault if there are problems.
   */
  @WebMethod
  List<FileWsResult> getVersions(
      @WebParam(name = "url") String url, @WebParam(name = "auId") String auId)
      throws LockssWebServicesFault;

  /**
   * Provides an indication of whether the content defined by a URL and Archival Unit is cached.
   *
   * @param url A String with the URL.
   * @param auId A String with the identifier (auid) of the archival unit.
   * @return a boolean with the indication.
   * @throws LockssWebServicesFault if there are problems.
   */
  @WebMethod
  boolean isUrlCached(@WebParam(name = "url") String url, @WebParam(name = "auId") String auId)
      throws LockssWebServicesFault;

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
  @WebMethod
  boolean isUrlVersionCached(
      @WebParam(name = "url") String url,
      @WebParam(name = "auId") String auId,
      @WebParam(name = "version") Integer version)
      throws LockssWebServicesFault;

  /**
   * Provides the content defined by a URL and Archival Unit.
   *
   * @param url A String with the URL.
   * @param auId A String with the identifier (auid) of the archival unit.
   * @return a ContentResult with the result of the operation.
   * @throws LockssWebServicesFault if there are problems.
   */
  @WebMethod
  ContentResult fetchFile(@WebParam(name = "url") String url, @WebParam(name = "auId") String auId)
      throws LockssWebServicesFault;

  /**
   * Provides the content defined by a URL, an Archival Unit and a version.
   *
   * @param url A String with the URL.
   * @param auId A String with the identifier (auid) of the archival unit.
   * @param version An Integer with the requested version of the content.
   * @return a ContentResult with the result of the operation.
   * @throws LockssWebServicesFault if there are problems.
   */
  @WebMethod
  ContentResult fetchVersionedFile(
      @WebParam(name = "url") String url,
      @WebParam(name = "auId") String auId,
      @WebParam(name = "version") Integer version)
      throws LockssWebServicesFault;
}
