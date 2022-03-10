/*

Copyright (c) 2000-2019 Board of Trustees of Leland Stanford Jr. University,
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

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import org.lockss.ws.entities.ContentResult;
import org.lockss.ws.entities.LockssWebServicesFault;

/** The Message Transmission Optimization Mechanism (MTOM) Content SOAP web service interface. */
@WebService
public interface MtomContentService {
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
