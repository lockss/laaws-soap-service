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
package org.lockss.ws.cxf;

import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.QName;
import org.apache.cxf.ext.logging.event.LogEvent;
import org.apache.cxf.ext.logging.event.LogEventSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.MarkerFactory;

/**
 * Implementation of org.apache.cxf.ext.logging.slf4j.Slf4jEventSender for
 * logging at level TRACE instead of level INFO.
 */
public class LockssSlf4jEventSender implements LogEventSender {
  /**
   * It is called by the Logging interceptor to send the fully populated message
   * to be logged.
   */
  @Override
  public void send(LogEvent event) {
    String cat = "org.apache.cxf.services."
	+ event.getPortTypeName().getLocalPart() + "." + event.getType();
    Logger log = LoggerFactory.getLogger(cat);
    Set<String> keys = new HashSet<>();

    try {
      put(keys, "Type", event.getType().toString());
      put(keys, "Address", event.getAddress());
      put(keys, "HttpMethod", event.getHttpMethod());
      put(keys, "Content-Type", event.getContentType());
      put(keys, "ResponseCode", event.getResponseCode());
      put(keys, "ExchangeId", event.getExchangeId());
      put(keys, "MessageId", event.getMessageId());
      if (event.getServiceName() != null) {
	put(keys, "ServiceName", localPart(event.getServiceName()));
	put(keys, "PortName", localPart(event.getPortName()));
	put(keys, "PortTypeName", localPart(event.getPortTypeName()));
      }
      if (event.getFullContentFile() != null) {
	put(keys, "FullContentFile",
	    event.getFullContentFile().getAbsolutePath());
      }
      put(keys, "Headers", event.getHeaders().toString());
      log.trace(MarkerFactory.getMarker(event.getServiceName() != null ?
	  "SOAP" : "REST"), getLogMessage(event));
    } finally {
      for (String key : keys) {
	MDC.remove(key);
      }
    }
  }

  private String localPart(QName name) {
    return name == null ? null : name.getLocalPart();
  }

  protected String getLogMessage(LogEvent event) {
    return event.getPayload();
  }

  private void put(Set<String> keys, String key, String value) {
    if (value != null) {
      MDC.put(key, value);
      keys.add(key);
    }
  }
}
