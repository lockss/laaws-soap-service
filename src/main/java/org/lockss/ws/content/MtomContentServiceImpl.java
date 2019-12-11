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

import java.io.ByteArrayInputStream;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.xml.ws.soap.MTOM;
import org.apache.cxf.jaxrs.ext.multipart.InputStreamDataSource;
import org.lockss.log.L4JLogger;
import org.lockss.ws.entities.ContentResult;
import org.lockss.ws.entities.LockssWebServicesFault;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/**
 * The Message Transmission Optimization Mechanism (MTOM) Content SOAP web
 * service implementation.
 */
@MTOM
@Service
public class MtomContentServiceImpl implements MtomContentService {
  private final static L4JLogger log = L4JLogger.getLogger();

  @Autowired
  private Environment env;

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
    log.debug2("url = {}, auId = {}", url, auId);

    if (url == null || url.isEmpty()) {
      throw new LockssWebServicesFault("Missing required URL");
    }

    return fetchVersionedFile(url, auId, null);
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
    log.debug2("url = {}, auId = {}, version = {}", url, auId, version);

    if (url == null || url.isEmpty()) {
      throw new LockssWebServicesFault("Missing required URL");
    }

    if ((auId == null || auId.isEmpty()) && version != null) {
      throw new LockssWebServicesFault("To fetch a specific version, the "
	  + "Archival Unit identifier (auId) is required");
    }

    try {
      // TODO: REPLACE THIS BLOCK WITH THE ACTUAL IMPLEMENTATION.
      ContentResult result = new ContentResult();

      Properties props = new Properties();
      props.put("someKey", "someValue");
      result.setProperties(props);

      result.setDataHandler(new DataHandler(new InputStreamDataSource(
	  new ByteArrayInputStream("text from fetchVersionedFile()".getBytes()),
	  "text/plain")));
      // TODO: END OF BLOCK TO BE REPLACED.

      log.debug2("result = {}", result);
      return result;
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }
}
