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
package org.lockss.ws;

import java.util.List;
import java.util.Map;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.lockss.log.L4JLogger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

/**
 * SOAP Service utility code.
 */
public class SoapServiceUtil {
  /** The URL of the Repository REST service. */
  public final static String REPO_SVC_URL_KEY = "repository.service.url";
  /** The URL of the Configuration REST service. */
  public final static String CONFIG_SVC_URL_KEY = "configuration.service.url";
  /** The URL of the Poller REST service. */
  public final static String POLLER_SVC_URL_KEY = "poller.service.url";
  /** The URL of the Metadata Extractor REST service. */
  public final static String MDX_SVC_URL_KEY = "metadataextractor.service.url";
  /** The URL of the Metadata REST service. */
  public final static String MDQ_SVC_URL_KEY = "metadata.service.url";

  private final static L4JLogger log = L4JLogger.getLogger();

  /**
   * Provides a newly-created REST template to access REST services.
   *
   * @return a RestTemplate with the REST template.
   */
  public static RestTemplate getRestTemplate() {
    log.debug2("Invoked.");

    RestTemplate restTemplate = new RestTemplate();

    restTemplate.setErrorHandler(new DefaultResponseErrorHandler(){
      protected boolean hasError(HttpStatus statusCode) {
	return false;
      }
    });

    log.debug2("Done.");
    return restTemplate;
  }

  /**
   * Adds the Authorization header in the current SOAP request message, if any,
   * to a passed set of HTTP headers.
   * 
   * @param httpHeaders An HttpHeaders with the initial HTTP headers.
   * @return an HttpHeaders with the resulting HTTP headers.
   */
  public static HttpHeaders addSoapCredentials(HttpHeaders httpHeaders) {
    log.debug2("httpHeaders = {}", httpHeaders);

    // Get the Authorization header from the SOAP request.
    String authHeaderValue = getSoapRequestAuthorizationHeader();

    // Check whether there is an Authorization header.
    if (authHeaderValue != null) {
      // Yes: Add it to the headers.
      httpHeaders.add("Authorization", authHeaderValue);
    }

    log.debug2("httpHeaders = {}", httpHeaders);
    return httpHeaders;
  }

  /**
   * Provides the Authorization header in the current SOAP request message, if
   * any.
   *
   * @return a String with the Authorization header.
   */
  public static String getSoapRequestAuthorizationHeader() {
    log.debug2("Invoked.");

    String authHeaderValue = null;

    // Get the headers from the SOAP request.
    Map<String, List<String>> soapHeaders =
	CastUtils.cast((Map<?, ?>)PhaseInterceptorChain.getCurrentMessage()
	    .get(Message.PROTOCOL_HEADERS));
    log.trace("soapHeaders = {}", soapHeaders);

    // Check whether there are headers.
    if (soapHeaders != null) {
      // Yes: Get any Authorization headers.
      List<String> authHeaders = soapHeaders.get("Authorization");
      log.trace("authHeaders = {}", authHeaders);

      // Check whether there is an Authorization header.
      if (authHeaders != null && !authHeaders.isEmpty()) {
	// Yes.
	authHeaderValue = authHeaders.get(0);
      }
    }

    log.debug2("authHeaderValue = {}", authHeaderValue);
    return authHeaderValue;
  }
}
