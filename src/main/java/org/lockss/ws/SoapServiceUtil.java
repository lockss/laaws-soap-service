/*

Copyright (c) 2000-2020 Board of Trustees of Leland Stanford Jr. University,
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

import java.net.URI;
import java.util.List;
import java.util.Map;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.lockss.log.L4JLogger;
import org.lockss.util.rest.RestUtil;
import org.lockss.util.rest.exception.LockssRestException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * SOAP Service utility code.
 */
public class SoapServiceUtil {
  /** The configuration key for the URL of the Repository REST service. */
  public final static String REPO_SVC_URL_KEY = "repository.service.url";
  /** The configuration key for the URL of the Configuration REST service. */
  public final static String CONFIG_SVC_URL_KEY = "configuration.service.url";
  /** The configuration key for the URL of the Poller REST service. */
  public final static String POLLER_SVC_URL_KEY = "poller.service.url";
  /** The configuration key for the URL of the Metadata Extractor REST service. */
  public final static String MDX_SVC_URL_KEY = "metadataextractor.service.url";
  /** The configuration key for the URL of the Metadata REST service. */
  public final static String MDQ_SVC_URL_KEY = "metadata.service.url";

  /** The configuration key for the connection timeout. */
  public final static String CONNECTION_TIMEOUT_KEY = "connection.timeout";
  /** The configuration key for the read timeout. */
  public final static String READ_TIMEOUT_KEY = "read.timeout";

  private final static L4JLogger log = L4JLogger.getLogger();

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

  /**
   * Makes a call to a REST service endpoint.
   * 
   * @param restTemplate     A RestTemplate used by Spring for synchronous
   *                         client-side HTTP access.
   * @param serviceUrl       A String with the URL of the service.
   * @param endPointPath     A String with the URI path to the endpoint.
   * @param uriVariables     A Map<String, String> with any variables to be
   *                         interpolated in the URI.
   * @param queryParams      A Map<String, String> with any query parameters.
   * @param httpMethod       An HttpMethod with HTTP method used to make the
   *                         call to the REST service.
   * @param authHeaderValue  A String with the value of the Authorization header
   *                         to be used to make the call, if any.
   * @param exceptionMessage A String with the message to be returned with any
   *                         exception.
   * @return a ResponseEntity<String> with the response from the REST service.
   * @throws LockssRestException if any problems arise in the call to the REST
   *                             service.
   */
  public static ResponseEntity<String> callRestServiceEndpoint(
      RestTemplate restTemplate, String serviceUrl, String endPointPath,
      Map<String, String> uriVariables, Map<String, String> queryParams,
      HttpMethod httpMethod, String authHeaderValue, String exceptionMessage)
	  throws LockssRestException {
    log.debug2("serviceUrl = {}", serviceUrl);
    log.debug2("endPointPath = {}", endPointPath);
    log.debug2("uriVariables = {}", uriVariables);
    log.debug2("queryParams = {}", queryParams);
    log.debug2("httpMethod = {}", httpMethod);
    log.debug2("authHeaderValue = {}", authHeaderValue);
    log.debug2("exceptionMessage = {}", exceptionMessage);

    // Create the URI of the request to the REST service.
    String uriString = serviceUrl + endPointPath;
    log.trace("uriString = {}", uriString);

    URI uri = RestUtil.getRestUri(uriString, uriVariables, queryParams);
    log.trace("uri = {}", uri);

    // Initialize the request headers.
    HttpHeaders requestHeaders = new HttpHeaders();

    // Check whether there are credentials to be sent.
    if (authHeaderValue != null && !authHeaderValue.isEmpty()) {
      // Yes.
      requestHeaders.set("Authorization", authHeaderValue);
    }

    log.trace("requestHeaders = {}", requestHeaders);

    // Create the request entity.
    HttpEntity<Void> requestEntity =
	new HttpEntity<>(null, requestHeaders);

    // Make the REST call.
    log.trace("Calling RestUtil.callRestService");
    return RestUtil.callRestService(restTemplate, uri, httpMethod,
	requestEntity, String.class, exceptionMessage);
  }
}
