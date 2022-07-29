/*

Copyright (c) 2000-2022, Board of Trustees of Leland Stanford Jr. University

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice,
this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice,
this list of conditions and the following disclaimer in the documentation
and/or other materials provided with the distribution.

3. Neither the name of the copyright holder nor the names of its contributors
may be used to endorse or promote products derived from this software without
specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.

*/
package org.lockss.ws.test;

import org.lockss.log.L4JLogger;
import org.lockss.spring.test.SpringLockssTestCase4;
import org.lockss.app.*;
import org.lockss.test.*;

import java.net.MalformedURLException;
import java.util.*;

import static org.lockss.ws.BaseServiceImpl.*;

public abstract class BaseSoapTest extends SpringLockssTestCase4 {
  private static L4JLogger log = L4JLogger.getLogger();

  private static Map<ServiceDescr,String> endpointMap = new HashMap<>();
  private static Properties bindProps = new Properties();
  static {
    endpointMap.put(ServiceDescr.SVC_CONFIG, "http://localhost:1");
    endpointMap.put(ServiceDescr.SVC_REPO, "http://localhost:2");
    endpointMap.put(ServiceDescr.SVC_POLLER, "http://localhost:3");
    endpointMap.put(ServiceDescr.SVC_CRAWLER, "http://localhost:4");
    endpointMap.put(ServiceDescr.SVC_MDX, "http://localhost:5");
    endpointMap.put(ServiceDescr.SVC_MDQ, "http://localhost:6");
  }

  static final String BINDINGS =
    "cfg=localhost:1;" +
    "repo=localhost:2;" +
    "poller=localhost:3;" +
    "crawler=localhost:4;" +
    "mdx=localhost:5;" +
    "mdq=localhost:6";

  protected void initBindings() throws MalformedURLException {
    log.fatal("Calling config");
    ConfigurationUtil.addFromArgs(LockssApp.PARAM_SERVICE_BINDINGS, BINDINGS);
    log.fatal("Called config");
//     proxy.setServiceEndpoints(endpointMap);
  }

  protected String getServiceEndpoint(ServiceDescr descr) {
    return endpointMap.get(descr);
  }
}
