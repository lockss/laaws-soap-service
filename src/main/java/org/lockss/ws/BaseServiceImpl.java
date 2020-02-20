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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.mail.MessagingException;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.lockss.laaws.rs.core.LockssRepositoryFactory;
import org.lockss.laaws.rs.core.RestLockssRepository;
import org.lockss.log.L4JLogger;
import org.lockss.util.Constants;
import org.lockss.util.auth.AuthUtil;
import org.lockss.util.rest.RestUtil;
import org.lockss.util.rest.exception.LockssRestException;
import org.lockss.util.rest.multipart.MultipartConnector;
import org.lockss.util.rest.multipart.MultipartResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
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

  /** The configuration key for the Repository collection identifier. */
  public final static String REPO_COLLECTION_KEY = "repository.collection";

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
    return RestUtil.getRestTemplate(getConnectionTimeout(), getReadTimeout());
  }

  /**
   * Provides the configured connection timeout in milliseconds.
   * 
   * @return a Long with the configured connection timeout in milliseconds.
   */
  protected Long getConnectionTimeout() {
    return env.getProperty(CONNECTION_TIMEOUT_KEY, Long.class,
	defaultConnectTimeout);
  }

  /**
   * Provides the configured read timeout in milliseconds.
   * 
   * @return a Long with the configured read timeout in milliseconds.
   */
  protected Long getReadTimeout() {
    return env.getProperty(READ_TIMEOUT_KEY, Long.class, defaultReadTimeout);
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
   * Provides access to the REST Repository service.
   * 
   * @return a RestLockssRepository that allows access to the REST Repository
   *         service.
   * @throws MalformedURLException if there are problems with the REST
   *                               Repository service URL.
   */
  protected RestLockssRepository getRestLockssRepository()
      throws MalformedURLException {
    String[] credentials = getSoapRequestCredentials();
    log.trace("credentials = [{}, ****]", credentials[0]);

    return LockssRepositoryFactory.createRestLockssRepository(
	new URL(env.getProperty(REPO_SVC_URL_KEY)), credentials[0],
	credentials[1]);
  }

  /**
   * Provides the credentials in the current SOAP request message, if any.
   *
   * @return a String[] with the credentials.
   */
  protected static String[] getSoapRequestCredentials() {
    log.debug2("Invoked.");

    String[] credentials = {null, null};

    String authHeaderValue = getSoapRequestAuthorizationHeader();
    log.debug2("authHeaderValue = {}", authHeaderValue);

    if (authHeaderValue != null) {
      credentials = AuthUtil.decodeBasicAuthorizationHeader(
	  getSoapRequestAuthorizationHeader());
      log.debug2("credentials = [{}, ****]", credentials[0]);
    }

    return credentials;
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
    return callRestServiceUri(serviceUrl + endPointPath, uriVariables,
	queryParams, httpMethod, body, exceptionMessage);
  }

  /**
   * Makes a call to a REST service URI.
   * 
   * @param uriString        A String with the URI of the request to the REST
   *                         service.
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
  protected <T> ResponseEntity<String> callRestServiceUri(String uriString,
      Map<String, String> uriVariables, Map<String, String> queryParams,
      HttpMethod httpMethod, T body, String exceptionMessage)
	  throws LockssRestException {
    return callRestServiceUri(uriString, uriVariables, queryParams, httpMethod,
	new HttpHeaders(), body, exceptionMessage);
  }

  /**
   * Makes a call to a REST service URI.
   * 
   * @param uriString        A String with the URI of the request to the REST
   *                         service.
   * @param uriVariables     A Map<String, String> with any variables to be
   *                         interpolated in the URI.
   * @param queryParams      A Map<String, String> with any query parameters.
   * @param httpMethod       An HttpMethod with HTTP method used to make the
   *                         call to the REST service.
   * @param requestHeaders   An HttpHeaders with HTTP request headers used to
   *                         make the call to the REST service.
   * @param body             A T with the contents of the body to be included
   *                         with the request, if any.
   * @param exceptionMessage A String with the message to be returned with any
   *                         exception.
   * @return a ResponseEntity<String> with the response from the REST service.
   * @throws LockssRestException if any problems arise in the call to the REST
   *                             service.
   */
  protected <T> ResponseEntity<String> callRestServiceUri(String uriString,
      Map<String, String> uriVariables, Map<String, String> queryParams,
      HttpMethod httpMethod, HttpHeaders requestHeaders, T body,
      String exceptionMessage) throws LockssRestException {
    log.debug2("uriString = {}", uriString);
    log.debug2("uriVariables = {}", uriVariables);
    log.debug2("queryParams = {}", queryParams);
    log.debug2("httpMethod = {}", httpMethod);
    log.debug2("body = {}", body);
    log.debug2("requestHeaders = {}", requestHeaders);
    log.debug2("exceptionMessage = {}", exceptionMessage);

    URI uri = RestUtil.getRestUri(uriString, uriVariables, queryParams);
    log.trace("uri = {}", uri);

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

  /**
   * Makes a call to a REST service endpoint that returns a multi-part response.
   * 
   * @param serviceUrl       A String with the URL of the service.
   * @param endPointPath     A String with the URI path to the endpoint.
   * @param uriVariables     A Map<String, String> with any variables to be
   *                         interpolated in the URI.
   * @param queryParams      A Map<String, String> with any query parameters.
   * @param requestHeaders   An HttpHeaders with HTTP request headers used to
   *                         make the call to the REST service.
   * @param httpMethod       An HttpMethod with HTTP method used to make the
   *                         call to the REST service.
   * @param body             A T with the contents of the body to be included
   *                         with the request, if any.
   * @return a MultipartResponse with the response from the REST service.
   * @throws LockssRestException if any problems arise in the call to the REST
   *                             service.
   */
  protected <T> MultipartResponse getMultipartResponse(String serviceUrl,
      String endPointPath, Map<String, String> uriVariables,
      Map<String, String> queryParams, HttpHeaders requestHeaders,
      HttpMethod httpMethod, T body) throws IOException, MessagingException {
    log.debug2("serviceUrl = {}", serviceUrl);
    log.debug2("endPointPath = {}", endPointPath);
    log.debug2("uriVariables = {}", uriVariables);
    log.debug2("queryParams = {}", queryParams);
    log.debug2("requestHeaders = {}", requestHeaders);
    log.debug2("httpMethod = {}", httpMethod);
    log.debug2("body = {}", body);

    URI uri = RestUtil.getRestUri(serviceUrl + endPointPath, uriVariables,
	queryParams);
    log.trace("uri = {}", uri);

    requestHeaders.setAccept(Arrays.asList(MediaType.MULTIPART_FORM_DATA,
	MediaType.APPLICATION_JSON));

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
    log.trace("Calling MultipartConnector.requestGet");
    return new MultipartConnector(uri, requestHeaders).request(httpMethod, body,
	getConnectionTimeout().intValue(), getReadTimeout().intValue());
  }

  // TODO: Remove once StrinGutil has been moved from lockss-core to
  // lockss-util.
  /**
   * Concatenate elements of collection into string, adding separators,
   * delimitig each element
   * @param c - Collection of object (on which toString() will be called)
   * @param separatorFirst - String to place before first element
   * @param separatorInner - String with which to separate elements
   * @param separatorLast - String to place after last element
   * @param sb - StringBuilder to write result into
   * @return sb
   */
  protected static StringBuilder separatedString(Collection c,
					      String separatorFirst,
					      String separatorInner,
					      String separatorLast,
					      StringBuilder sb) {
    if (c == null) {
      return sb;
    }
    Iterator iter = c.iterator();
    boolean first = true;
    String NULL_OBJECT_PRINTABLE_TEXT = "(null)";
    while (iter.hasNext()) {
      if (first) {
	first = false;
	sb.append(separatorFirst);
      } else {
	sb.append(separatorInner);
      }
      Object obj = iter.next();
      sb.append(obj == null ? NULL_OBJECT_PRINTABLE_TEXT : obj.toString());
    }
    if (!first) {
      sb.append(separatorLast);
    }
    return sb;
  }

  // TODO: Remove once StringUtil has been moved from lockss-core to
  // lockss-util.
  /**
   * Test whether a string is null or the empty string
   * @param s the string
   * @return true if s is null or the empty string
   */
  protected static boolean isNullString(String s) {
    return s == null || s.length() == 0;
  }
}
