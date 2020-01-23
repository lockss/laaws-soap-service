/*

 Copyright (c) 2000-2020 Board of Trustees of Leland Stanford Jr. University,
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
package org.lockss.ws;

import java.net.URI;
import java.util.List;
import java.util.Map;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.lockss.log.L4JLogger;
import org.lockss.util.Constants;
import org.lockss.util.rest.RestUtil;
import org.lockss.util.rest.exception.LockssRestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * Base class for the various SOAP web service implementations.
 */
public abstract class BaseServiceImpl {
  /** The configuration key for the URL of the Repository REST service. */
  public final static String REPO_SVC_URL_KEY = "repository.service.url";
  /** The configuration key for the URL of the Configuration REST service. */
  public final static String CONFIG_SVC_URL_KEY = "configuration.service.url";
  /** The configuration key for the URL of the Poller REST service. */
  public final static String POLLER_SVC_URL_KEY = "poller.service.url";
  /** The configuration key for the URL of the Metadata Extractor REST service.
   */
  public final static String MDX_SVC_URL_KEY = "metadataextractor.service.url";
  /** The configuration key for the URL of the Metadata REST service. */
  public final static String MDQ_SVC_URL_KEY = "metadata.service.url";

  /** The configuration key for the connection timeout. */
  public final static String CONNECTION_TIMEOUT_KEY = "connection.timeout";
  /** The configuration key for the read timeout. */
  public final static String READ_TIMEOUT_KEY = "read.timeout";

  private final static L4JLogger log = L4JLogger.getLogger();

  @Autowired
  protected Environment env;

  // Default timeouts.
  private long defaultConnectTimeout = 10 * Constants.SECOND;
  private long defaultReadTimeout = 120 * Constants.SECOND;

  /**
   * Provides the customized template used by Spring for synchronous client-side
   * HTTP access.
   * 
   * @return a RestTemplate with the customized Spring template.
   */
  protected RestTemplate getCustomRestTemplate() {
    return RestUtil.getRestTemplate(env.getProperty(CONNECTION_TIMEOUT_KEY,
	Long.class, defaultConnectTimeout),
	env.getProperty(READ_TIMEOUT_KEY, Long.class, defaultReadTimeout));
  }

  /**
   * Provides the Authorization header in the current SOAP request message, if
   * any.
   *
   * @return a String with the Authorization header.
   */
  protected static String getSoapRequestAuthorizationHeader() {
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
   * @param serviceUrl       A String with the URL of the service.
   * @param endPointPath     A String with the URI path to the endpoint.
   * @param uriVariables     A Map<String, String> with any variables to be
   *                         interpolated in the URI.
   * @param queryParams      A Map<String, String> with any query parameters.
   * @param httpMethod       An HttpMethod with HTTP method used to make the
   *                         call to the REST service.
   * @param body             A T with the contents of the body to be included
   *                         with the request, if any.
   * @param exceptionMessage A String with the message to be returned with any
   *                         exception.
   * @return a ResponseEntity<String> with the response from the REST service.
   * @throws LockssRestException if any problems arise in the call to the REST
   *                             service.
   */
  protected <T> ResponseEntity<String> callRestServiceEndpoint(
      String serviceUrl, String endPointPath, Map<String, String> uriVariables,
      Map<String, String> queryParams, HttpMethod httpMethod, T body,
      String exceptionMessage) throws LockssRestException {
    log.debug2("serviceUrl = {}", serviceUrl);
    log.debug2("endPointPath = {}", endPointPath);
    log.debug2("uriVariables = {}", uriVariables);
    log.debug2("queryParams = {}", queryParams);
    log.debug2("httpMethod = {}", httpMethod);
    log.debug2("body = {}", body);
    log.debug2("exceptionMessage = {}", exceptionMessage);

    // Create the URI of the request to the REST service.
    String uriString = serviceUrl + endPointPath;
    log.trace("uriString = {}", uriString);

    URI uri = RestUtil.getRestUri(uriString, uriVariables, queryParams);
    log.trace("uri = {}", uri);

    // Initialize the request headers.
    HttpHeaders requestHeaders = new HttpHeaders();

    // Get any incoming authorization header with credentials to be passed to
    // the REST service.
    String authHeaderValue = getSoapRequestAuthorizationHeader();
    log.trace("authHeaderValue = {}", authHeaderValue);

    // Check whether there are credentials to be sent.
    if (authHeaderValue != null && !authHeaderValue.isEmpty()) {
      // Yes: Add them to the request.
      requestHeaders.set("Authorization", authHeaderValue);
    }

    log.trace("requestHeaders = {}", requestHeaders);

    // Make the REST call.
    log.trace("Calling RestUtil.callRestService");
    return RestUtil.callRestService(getCustomRestTemplate(), uri, httpMethod,
	new HttpEntity<T>(body, requestHeaders), String.class,
	exceptionMessage);
  }
}
