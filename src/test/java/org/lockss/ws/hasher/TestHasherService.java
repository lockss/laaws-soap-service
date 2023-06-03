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
package org.lockss.ws.hasher;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lockss.app.ServiceDescr;
import org.lockss.log.L4JLogger;
import org.lockss.util.rest.RestResponseErrorBody;
import org.lockss.util.rest.RestUtil;
import org.lockss.ws.entities.HasherWsAsynchronousResult;
import org.lockss.ws.entities.HasherWsParams;
import org.lockss.ws.entities.HasherWsResult;
import org.lockss.ws.entities.LockssWebServicesFault;
import org.lockss.ws.test.BaseSoapTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.ExpectedCount;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"security.basic.enabled=false"})
public class TestHasherService extends BaseSoapTest {
  private static final L4JLogger log = L4JLogger.getLogger();

  private static final String TARGET_NAMESPACE = "http://hasher.ws.lockss.org/";
  private static final String SERVICE_NAME = "HasherServiceImplService";
  private static final String ENDPOINT_NAME = "HasherService";

  private static final String CRLF = "\r\n";

  // HasherStatus.RequestError.toString()
  private static final String REQUEST_ERROR = "RequestError";

  // FIXME: Blank mock REST error response
  private static final RestResponseErrorBody.RestResponseError blankError =
      new RestResponseErrorBody.RestResponseError();

  private HasherService proxy;

  @Before
  public void init() throws Exception {
    setUpMultipartFormConverter();

    proxy = setUpProxyAndCommonTestEnv(TARGET_NAMESPACE,
                                       ENDPOINT_NAME, SERVICE_NAME,
                                       HasherService.class);
  }

  /**
   * Test for {@link HasherService#hash(HasherWsParams)}.
   */
  @Test
  public void testHash() throws Exception {
    //// Test bad or no auth error ("401 Unauthorized") handling
    {
      HasherWsParams params = new HasherWsParams();
      params.setAuId("auid1");

      Map<String, String> queryParams = new HashMap<>();
      queryParams.put("isAsynchronous", "false");

      // REST API endpoint of operation we're testing
      URI restEndpoint = RestUtil.getRestUri(getServiceEndpoint(ServiceDescr.SVC_POLLER) + "/ws/hashes", null,
          queryParams);

      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.PUT))
          .andExpect(header("Accept", "multipart/form-data, application/json"))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andExpect(content().contentType("application/json"))
          .andExpect(content().string(mapper.writeValueAsString(params)))
          .andRespond(withStatus(HttpStatus.UNAUTHORIZED)
              .contentType(MediaType.APPLICATION_JSON_UTF8)
              .body(mapper.writeValueAsString(blankError)));

      // Make the call through SOAP
      assertThrows(LockssWebServicesFault.class,
          () -> proxy.hash(params),
          "401 Unauthorized");

      mockRestServer.verify();
      mockRestServer.reset();
    }

    //// Test bad User Role error ("Forbidden") handling
    {
      HasherWsParams params = new HasherWsParams();
      params.setAuId("auid1");

      Map<String, String> queryParams = new HashMap<>();
      queryParams.put("isAsynchronous", "false");

      // REST API endpoint of operation we're testing
      URI restEndpoint = RestUtil.getRestUri(getServiceEndpoint(ServiceDescr.SVC_POLLER) + "/ws/hashes", null,
          queryParams);

      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.PUT))
          .andExpect(header("Accept", "multipart/form-data, application/json"))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andExpect(content().contentType("application/json"))
          .andExpect(content().string(mapper.writeValueAsString(params)))
          .andRespond(withStatus(HttpStatus.FORBIDDEN)
              .contentType(MediaType.APPLICATION_JSON)
              .body(mapper.writeValueAsString(blankError)));

      // Make the call through SOAP
      assertThrows(LockssWebServicesFault.class,
          () -> proxy.hash(params),
          "403 Forbidden");

      mockRestServer.verify();
      mockRestServer.reset();
    }

    //// Test unforeseen exception ("500 Internal Server Error") handling
    {
      HasherWsParams params = new HasherWsParams();
      params.setAuId("auid1");

      Map<String, String> queryParams = new HashMap<>();
      queryParams.put("isAsynchronous", "false");

      // REST API endpoint of operation we're testing
      URI restEndpoint = RestUtil.getRestUri(getServiceEndpoint(ServiceDescr.SVC_POLLER) + "/ws/hashes", null,
          queryParams);

      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.PUT))
          .andExpect(header("Accept", "multipart/form-data, application/json"))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andExpect(content().contentType("application/json"))
          .andExpect(content().string(mapper.writeValueAsString(params)))
          .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
              .contentType(MediaType.APPLICATION_JSON)
              .body(mapper.writeValueAsString(blankError)));

      // Make the call through SOAP
      assertThrows(LockssWebServicesFault.class,
          () -> proxy.hash(params),
          "500 Internal Server Error");

      mockRestServer.verify();
      mockRestServer.reset();
    }

    //// Test success
    {
      HasherWsParams params = new HasherWsParams();
      params.setAuId("auid1");

      Map<String, String> queryParams = new HashMap<>();
      queryParams.put("isAsynchronous", "false");

      // REST API endpoint of operation we're testing
      URI restEndpoint = RestUtil.getRestUri(getServiceEndpoint(ServiceDescr.SVC_POLLER) + "/ws/hashes", null,
          queryParams);

      // This map simulates the map built by the Poller service from a HasherResult object
      Map<String, Object> resultProps = new HashMap<>();

      resultProps.put("requestId", "noRequestId"); // DEFAULT_REQUEST_ID when isAsynchronous=false
      resultProps.put("startTime", 12345L);

      resultProps.put("recordFileName", "testRecordFileName");
      resultProps.put("blockFileName", "testBlockFileName");

      resultProps.put("errorMessage", "testErrorMessage");
      resultProps.put("status", "Done");

      resultProps.put("hashResult", "testHashResult".getBytes(StandardCharsets.UTF_8));

      resultProps.put("bytesHashed", 12345L);
      resultProps.put("filesHashed", 12345);
      resultProps.put("elapsedTime", 12345L);

      String responseBody = "--12345\r\n" +
          "Content-Disposition: form-data; name=\"testRequestId\"" + CRLF +
          "Content-Type: application/json" + CRLF +
          CRLF +
          mapper.writeValueAsString(resultProps) + CRLF +
          "--12345\r\n" +
          "Content-Disposition: form-data; name=\"testRequestId-Block\"" + CRLF +
          "Content-Length: 5" + CRLF +
          // Content-Type is parsed on the client-side to create an AttachmentDataSource
          // but it is not provided by the Poller service
          CRLF +
          "test1" + CRLF +
          "--12345\r\n" +
          "Content-Disposition: form-data; name=\"testRequestId-Record\"" + CRLF +
          "Content-Length: 5" + CRLF +
          // Content-Type is parsed on the client-side to create an AttachmentDataSource
          // but it is not provided by the Poller service
          CRLF +
          "test2" + CRLF +
          "--12345--\r\n";

      HttpHeaders responseHeaders = new HttpHeaders();
      responseHeaders.setContentLength(responseBody.length());

      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.PUT))
          .andExpect(header("Accept", "multipart/form-data, application/json"))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andExpect(content().contentType("application/json"))
          .andExpect(content().string(mapper.writeValueAsString(params)))
          .andRespond(withStatus(HttpStatus.OK)
              .contentType(MediaType.parseMediaType("multipart/form-data; boundary=12345\n"))
              .headers(responseHeaders)
              .body(responseBody));

      // Make the call through SOAP
      HasherWsResult result = proxy.hash(params);

      // This is always sent over the wire by the REST server but ignored when isAsynchronous=false
      // assertEquals(resultProps.get("requestId"), result.getRequestId());

      assertEquals(resultProps.get("startTime"), result.getStartTime());
      assertSameBytes(new ByteArrayInputStream((byte[])resultProps.get("hashResult")),
          new ByteArrayInputStream(result.getHashResult()));
      assertEquals(resultProps.get("errorMessage"), result.getErrorMessage());
      assertEquals(resultProps.get("status"), result.getStatus());
      assertEquals(resultProps.get("bytesHashed"), result.getBytesHashed());
      assertEquals(resultProps.get("filesHashed"), result.getFilesHashed());
      assertEquals(resultProps.get("elapsedTime"), result.getElapsedTime());

      // Assert "block" and "record" file names and contents
      assertEquals(resultProps.get("blockFileName"), result.getBlockFileName());
      assertEquals(resultProps.get("recordFileName"), result.getRecordFileName());
      assertInputStreamMatchesString("test1", result.getBlockFileDataHandler().getInputStream());
      assertInputStreamMatchesString("test2", result.getRecordFileDataHandler().getInputStream());

      mockRestServer.verify();
      mockRestServer.reset();
    }
  }

  /**
   * Test for {@link HasherService#hashAsynchronously(HasherWsParams)}.
   */
  @Test
  public void testHashAsynchronously() throws Exception {
    //// Test bad or no auth error ("401 Unauthorized") handling
    {
      HasherWsParams params = new HasherWsParams();
      params.setAuId("auid1");

      Map<String, String> queryParams = new HashMap<>();
      queryParams.put("isAsynchronous", "true");

      // REST API endpoint of operation we're testing
      URI restEndpoint = RestUtil.getRestUri(getServiceEndpoint(ServiceDescr.SVC_POLLER) + "/ws/hashes", null,
          queryParams);

      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.PUT))
          .andExpect(header("Accept", "multipart/form-data, application/json"))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andExpect(content().contentType("application/json"))
          .andExpect(content().string(mapper.writeValueAsString(params)))
          .andRespond(withStatus(HttpStatus.UNAUTHORIZED)
              .contentType(MediaType.APPLICATION_JSON)
              .body(mapper.writeValueAsString(blankError)));

      // Make the call through SOAP
      assertThrows(LockssWebServicesFault.class,
          () -> proxy.hashAsynchronously(params),
          "401 Unauthorized");

      mockRestServer.verify();
      mockRestServer.reset();
    }

    //// Test bad User Role error ("Forbidden") handling
    {
      HasherWsParams params = new HasherWsParams();
      params.setAuId("auid1");

      Map<String, String> queryParams = new HashMap<>();
      queryParams.put("isAsynchronous", "true");

      // REST API endpoint of operation we're testing
      URI restEndpoint = RestUtil.getRestUri(getServiceEndpoint(ServiceDescr.SVC_POLLER) + "/ws/hashes", null,
          queryParams);

      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.PUT))
          .andExpect(header("Accept", "multipart/form-data, application/json"))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andExpect(content().contentType("application/json;charset"))
          .andExpect(content().string(mapper.writeValueAsString(params)))
          .andRespond(withStatus(HttpStatus.FORBIDDEN)
              .contentType(MediaType.APPLICATION_JSON)
              .body(mapper.writeValueAsString(blankError)));

      // Make the call through SOAP
      assertThrows(LockssWebServicesFault.class,
          () -> proxy.hashAsynchronously(params),
          "403 Forbidden");

      mockRestServer.verify();
      mockRestServer.reset();
    }

    //// Test unforeseen exception ("500 Internal Server Error") handling
    {
      HasherWsParams params = new HasherWsParams();
      params.setAuId("auid1");

      Map<String, String> queryParams = new HashMap<>();
      queryParams.put("isAsynchronous", "true");

      // REST API endpoint of operation we're testing
      URI restEndpoint = RestUtil.getRestUri(getServiceEndpoint(ServiceDescr.SVC_POLLER) + "/ws/hashes", null,
          queryParams);

      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.PUT))
          .andExpect(header("Accept", "multipart/form-data, application/json"))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andExpect(content().contentType("application/json"))
          .andExpect(content().string(mapper.writeValueAsString(params)))
          .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
              .contentType(MediaType.APPLICATION_JSON)
              .body(mapper.writeValueAsString(blankError)));

      // Make the call through SOAP
      assertThrows(LockssWebServicesFault.class,
          () -> proxy.hashAsynchronously(params),
          "500 Internal Server Error");

      mockRestServer.verify();
      mockRestServer.reset();
    }

    //// Test success
    {
      HasherWsParams params = new HasherWsParams();
      params.setAuId("auid1");

      Map<String, String> queryParams = new HashMap<>();
      queryParams.put("isAsynchronous", "true");

      // REST API endpoint of operation we're testing
      URI restEndpoint = RestUtil.getRestUri(getServiceEndpoint(ServiceDescr.SVC_POLLER) + "/ws/hashes", null,
          queryParams);

      // This map simulates the map built by the Poller service from a HasherResult object
      Map<String, Object> resultProps = new HashMap<>();

      resultProps.put("requestId", "testRequestId");
      resultProps.put("startTime", 12345L);

      resultProps.put("recordFileName", "testRecordFileName");
      resultProps.put("blockFileName", "testBlockFileName");

      resultProps.put("errorMessage", "testErrorMessage");
      resultProps.put("status", "Done");

      resultProps.put("hashResult", "testHashResult".getBytes(StandardCharsets.UTF_8));

      resultProps.put("bytesHashed", 12345L);
      resultProps.put("filesHashed", 12345);
      resultProps.put("elapsedTime", 12345L);

      String responseBody = "--12345\r\n" +
          "Content-Disposition: form-data; name=\"testRequestId\"" + CRLF +
          "Content-Type: application/json" + CRLF +
          CRLF +
          mapper.writeValueAsString(resultProps) + CRLF +
          "--12345\r\n" +
          "Content-Disposition: form-data; name=\"testRequestId-Block\"" + CRLF +
          "Content-Length: 5" + CRLF +
          // Content-Type is parsed on the client-side to create an AttachmentDataSource
          // but it is not provided by the Poller service
          CRLF +
          "test1" + CRLF +
          "--12345\r\n" +
          "Content-Disposition: form-data; name=\"testRequestId-Record\"" + CRLF +
          "Content-Length: 5" + CRLF +
          // Content-Type is parsed on the client-side to create an AttachmentDataSource
          // but it is not provided by the Poller service
          CRLF +
          "test2" + CRLF +
          "--12345--\r\n";

      HttpHeaders responseHeaders = new HttpHeaders();
      responseHeaders.setContentLength(responseBody.length());

      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.PUT))
          .andExpect(header("Accept", "multipart/form-data, application/json"))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andExpect(content().contentType("application/json"))
          .andExpect(content().string(mapper.writeValueAsString(params)))
          .andRespond(withStatus(HttpStatus.OK)
              .contentType(MediaType.parseMediaType("multipart/form-data; boundary=12345\n"))
              .headers(responseHeaders)
              .body(responseBody));

      // Make the call through SOAP
      HasherWsAsynchronousResult result = proxy.hashAsynchronously(params);

      assertEquals(resultProps.get("requestId"), result.getRequestId());
      assertEquals(resultProps.get("startTime"), result.getStartTime());
      assertSameBytes(new ByteArrayInputStream((byte[])resultProps.get("hashResult")),
          new ByteArrayInputStream(result.getHashResult()));
      assertEquals(resultProps.get("errorMessage"), result.getErrorMessage());
      assertEquals(resultProps.get("status"), result.getStatus());
      assertEquals(resultProps.get("bytesHashed"), result.getBytesHashed());
      assertEquals(resultProps.get("filesHashed"), result.getFilesHashed());
      assertEquals(resultProps.get("elapsedTime"), result.getElapsedTime());

      // Assert "block" and "record" file names and contents
      assertEquals(resultProps.get("blockFileName"), result.getBlockFileName());
      assertEquals(resultProps.get("recordFileName"), result.getRecordFileName());
      assertInputStreamMatchesString("test1", result.getBlockFileDataHandler().getInputStream());
      assertInputStreamMatchesString("test2", result.getRecordFileDataHandler().getInputStream());

      mockRestServer.verify();
      mockRestServer.reset();
    }
  }

  /**
   * Test for {@link HasherService#getAsynchronousHashResult(String)}.
   */
  @Test
  public void testGetAsynchronousHashResult() throws Exception {
    //// Test null requestId error ("400 Bad Request") handling
    {
      String requestId = "requestId";

      // REST API endpoint of operation we're testing
      URI restEndpoint =
          RestUtil.getRestUri(getServiceEndpoint(ServiceDescr.SVC_POLLER) + "/ws/hashes/requests/" + requestId, null,
              null);

      String message = "Must supply request identifier";

      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.GET))
          .andExpect(header("Accept", "multipart/form-data, application/json"))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andRespond(withStatus(HttpStatus.BAD_REQUEST)
              .contentType(MediaType.TEXT_PLAIN)
              .body(message));

          // Make the call through SOAP
      assertThrows(LockssWebServicesFault.class,
          () -> proxy.getAsynchronousHashResult(requestId),
          "400 Bad Request");

      mockRestServer.verify();
      mockRestServer.reset();
    }

    //// Test bad or no auth error ("401 Unauthorized") handling
    {
      String requestId = "requestId";

      // REST API endpoint of operation we're testing
      URI restEndpoint =
          RestUtil.getRestUri(getServiceEndpoint(ServiceDescr.SVC_POLLER) + "/ws/hashes/requests/" + requestId, null,
              null);

      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.GET))
          .andExpect(header("Accept", "multipart/form-data, application/json"))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andRespond(withStatus(HttpStatus.UNAUTHORIZED)
              .contentType(MediaType.TEXT_PLAIN)); // Suppress JSON parsing of empty body

      // Make the call through SOAP
      assertThrows(LockssWebServicesFault.class,
          () -> proxy.getAsynchronousHashResult(requestId),
          "401 Unauthorized");

      mockRestServer.verify();
      mockRestServer.reset();
    }

    //// Test bad User Role error ("Forbidden") handling
    {
      String requestId = "requestId";

      // REST API endpoint of operation we're testing
      URI restEndpoint =
          RestUtil.getRestUri(getServiceEndpoint(ServiceDescr.SVC_POLLER) + "/ws/hashes/requests/" + requestId, null,
              null);

      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.GET))
          .andExpect(header("Accept", "multipart/form-data, application/json"))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andRespond(withStatus(HttpStatus.FORBIDDEN)
              .contentType(MediaType.TEXT_PLAIN)); // Suppress JSON parsing of empty body

      // Make the call through SOAP
      assertThrows(LockssWebServicesFault.class,
          () -> proxy.getAsynchronousHashResult(requestId),
          "403 Forbidden");

      mockRestServer.verify();
      mockRestServer.reset();
    }

    //// Test missing request error ("404 Not Found") handling
    {
      String requestId = "requestId";

      // REST API endpoint of operation we're testing
      URI restEndpoint =
          RestUtil.getRestUri(getServiceEndpoint(ServiceDescr.SVC_POLLER) + "/ws/hashes/requests/" + requestId, null,
              null);

      String message = "Cannot find asynchronous hash request '" + requestId + "'";

      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.GET))
          .andExpect(header("Accept", "multipart/form-data, application/json"))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andRespond(withStatus(HttpStatus.NOT_FOUND)
              .contentType(MediaType.TEXT_PLAIN)
              .body(message));

      // Make the call through SOAP
      assertThrows(LockssWebServicesFault.class,
          () -> proxy.getAsynchronousHashResult(requestId),
          "404 Not Found");

      mockRestServer.verify();
      mockRestServer.reset();
    }

    //// Test unforeseen exception ("500 Internal Server Error") handling
    {
      String requestId = "requestId";

      // REST API endpoint of operation we're testing
      URI restEndpoint =
          RestUtil.getRestUri(getServiceEndpoint(ServiceDescr.SVC_POLLER) + "/ws/hashes/requests/" + requestId, null,
              null);

      String message = "Cannot getHash() for requestId = '" + requestId + "'";

      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.GET))
          .andExpect(header("Accept", "multipart/form-data, application/json"))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
              .contentType(MediaType.TEXT_PLAIN)
              .body(message));

      // Make the call through SOAP
      assertThrows(LockssWebServicesFault.class,
          () -> proxy.getAsynchronousHashResult(requestId),
          "500 Internal Server Error");

      mockRestServer.verify();
      mockRestServer.reset();
    }

    //// Test success
    {
      String requestId = "requestId";

      // REST API endpoint of operation we're testing
      URI restEndpoint =
          RestUtil.getRestUri(getServiceEndpoint(ServiceDescr.SVC_POLLER) + "/ws/hashes/requests/" + requestId, null,
              null);

      // This map simulates the map built by the Poller service from a HasherResult object
      Map<String, Object> resultProps = new HashMap<>();

      resultProps.put("requestId", "testRequestId");
      resultProps.put("startTime", 12345L);

      resultProps.put("recordFileName", "testRecordFileName");
      resultProps.put("blockFileName", "testBlockFileName");

      resultProps.put("errorMessage", "testErrorMessage");
      resultProps.put("status", "Done");

      resultProps.put("hashResult", "testHashResult".getBytes(StandardCharsets.UTF_8));

      resultProps.put("bytesHashed", 12345L);
      resultProps.put("filesHashed", 12345);
      resultProps.put("elapsedTime", 12345L);

      String responseBody = "--12345\r\n" +
          "Content-Disposition: form-data; name=\"testRequestId\"" + CRLF +
          "Content-Type: application/json" + CRLF +
          CRLF +
          mapper.writeValueAsString(resultProps) + CRLF +
          "--12345\r\n" +
          "Content-Disposition: form-data; name=\"testRequestId-Block\"" + CRLF +
          "Content-Length: 5" + CRLF +
          // Content-Type is parsed on the client-side to create an AttachmentDataSource
          // but it is not provided by the Poller service
          CRLF +
          "test1" + CRLF +
          "--12345\r\n" +
          "Content-Disposition: form-data; name=\"testRequestId-Record\"" + CRLF +
          "Content-Length: 5" + CRLF +
          // Content-Type is parsed on the client-side to create an AttachmentDataSource
          // but it is not provided by the Poller service
          CRLF +
          "test2" + CRLF +
          "--12345--\r\n";

      HttpHeaders responseHeaders = new HttpHeaders();
      responseHeaders.setContentLength(responseBody.length());

      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.GET))
          .andExpect(header("Accept", "multipart/form-data, application/json"))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andRespond(withStatus(HttpStatus.OK)
              .contentType(MediaType.parseMediaType("multipart/form-data; boundary=12345\n"))
              .headers(responseHeaders)
              .body(responseBody));

      // Make the call through SOAP
      HasherWsAsynchronousResult result = proxy.getAsynchronousHashResult(requestId);

      assertEquals(resultProps.get("requestId"), result.getRequestId());
      assertEquals(resultProps.get("startTime"), result.getStartTime());
      assertSameBytes(new ByteArrayInputStream((byte[])resultProps.get("hashResult")),
          new ByteArrayInputStream(result.getHashResult()));
      assertEquals(resultProps.get("errorMessage"), result.getErrorMessage());
      assertEquals(resultProps.get("status"), result.getStatus());
      assertEquals(resultProps.get("bytesHashed"), result.getBytesHashed());
      assertEquals(resultProps.get("filesHashed"), result.getFilesHashed());
      assertEquals(resultProps.get("elapsedTime"), result.getElapsedTime());

      // Assert "block" and "record" file names and contents
      assertEquals(resultProps.get("blockFileName"), result.getBlockFileName());
      assertEquals(resultProps.get("recordFileName"), result.getRecordFileName());
      assertInputStreamMatchesString("test1", result.getBlockFileDataHandler().getInputStream());
      assertInputStreamMatchesString("test2", result.getRecordFileDataHandler().getInputStream());

      mockRestServer.verify();
      mockRestServer.reset();
    }
  }

  /**
   * Test for {@link HasherService#getAllAsynchronousHashResults()}.
   */
  @Test
  public void testGetAllAsynchronousHashResults() throws Exception {
    //// Test bad or no auth error ("401 Unauthorized") handling
    {
      // REST API endpoint of operation we're testing
      URI restEndpoint = RestUtil.getRestUri(getServiceEndpoint(ServiceDescr.SVC_POLLER) + "/ws/hashes", null, null);

      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.GET))
          .andExpect(header("Accept", "multipart/form-data, application/json"))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andRespond(withStatus(HttpStatus.UNAUTHORIZED)
              .contentType(MediaType.TEXT_PLAIN)); // Suppress JSON parsing of empty body

      // Make the call through SOAP
      assertThrows(LockssWebServicesFault.class,
          () -> proxy.getAllAsynchronousHashResults(),
          "401 Unauthorized");

      mockRestServer.verify();
      mockRestServer.reset();
    }

    //// Test bad User Role error ("Forbidden") handling
    {
      // REST API endpoint of operation we're testing
      URI restEndpoint = RestUtil.getRestUri(getServiceEndpoint(ServiceDescr.SVC_POLLER) + "/ws/hashes", null, null);

      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.GET))
          .andExpect(header("Accept", "multipart/form-data, application/json"))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
              .contentType(MediaType.TEXT_PLAIN)); // Suppress JSON parsing of empty body

      // Make the call through SOAP
      assertThrows(LockssWebServicesFault.class,
          () -> proxy.getAllAsynchronousHashResults(),
          "403 Forbidden");

      mockRestServer.verify();
      mockRestServer.reset();
    }

    //// Test unforeseen exception ("500 Internal Server Error") handling
    {
      // REST API endpoint of operation we're testing
      URI restEndpoint = RestUtil.getRestUri(getServiceEndpoint(ServiceDescr.SVC_POLLER) + "/ws/hashes", null, null);

      String message = "Cannot getAllHashes()";

      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.GET))
          .andExpect(header("Accept", "multipart/form-data, application/json"))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
              .contentType(MediaType.TEXT_PLAIN)
              .body(message));

      // Make the call through SOAP
      assertThrows(LockssWebServicesFault.class,
          () -> proxy.getAllAsynchronousHashResults(),
          "500 Internal Server Error");

      mockRestServer.verify();
      mockRestServer.reset();
    }

    //// Test success
    {
      // REST API endpoint of operation we're testing
      URI restEndpoint = RestUtil.getRestUri(getServiceEndpoint(ServiceDescr.SVC_POLLER) + "/ws/hashes", null, null);

      // This map simulates the map built by the Poller service from a HasherResult object
      Map<String, Object> resultProps1 = new HashMap<>();

      resultProps1.put("requestId", "testRequestId1");
      resultProps1.put("startTime", 12345L);

      resultProps1.put("recordFileName", "testRecordFileName1");
      resultProps1.put("blockFileName", "testBlockFileName1");

      resultProps1.put("errorMessage", "testErrorMessage1");
      resultProps1.put("status", "Done");

      resultProps1.put("hashResult", "testHashResult1".getBytes(StandardCharsets.UTF_8));

      resultProps1.put("bytesHashed", 12345L);
      resultProps1.put("filesHashed", 12345);
      resultProps1.put("elapsedTime", 12345L);

      // This map simulates the map built by the Poller service from a HasherResult object
      Map<String, Object> resultProps2 = new HashMap<>();

      resultProps2.put("requestId", "testRequestId2");
      resultProps2.put("startTime", 67890L);

      resultProps2.put("recordFileName", "testRecordFileName2");
      resultProps2.put("blockFileName", "testBlockFileName2");

      resultProps2.put("errorMessage", "testErrorMessage2");
      resultProps2.put("status", "Done");

      resultProps2.put("hashResult", "testHashResult2".getBytes(StandardCharsets.UTF_8));

      resultProps2.put("bytesHashed", 67890L);
      resultProps2.put("filesHashed", 67890);
      resultProps2.put("elapsedTime", 67890L);

      String responseBody = "--12345\r\n" +
          "Content-Disposition: form-data; name=\"testRequestId1\"" + CRLF +
          "Content-Type: application/json" + CRLF +
          CRLF +
          mapper.writeValueAsString(resultProps1) + CRLF +
          "--12345\r\n" +
          "Content-Disposition: form-data; name=\"testRequestId1-Block\"" + CRLF +
          "Content-Length: 5" + CRLF +
          // Content-Type is parsed on the client-side to create an AttachmentDataSource
          // but it is not provided by the Poller service
          CRLF +
          "test1" + CRLF +
          "--12345\r\n" +
          "Content-Disposition: form-data; name=\"testRequestId1-Record\"" + CRLF +
          "Content-Length: 5" + CRLF +
          // Content-Type is parsed on the client-side to create an AttachmentDataSource
          // but it is not provided by the Poller service
          CRLF +
          "test2" + CRLF +
          "--12345\r\n" +
          "Content-Disposition: form-data; name=\"testRequestId2\"" + CRLF +
          "Content-Type: application/json" + CRLF +
          CRLF +
          mapper.writeValueAsString(resultProps2) + CRLF +
          "--12345\r\n" +
          "Content-Disposition: form-data; name=\"testRequestId2-Block\"" + CRLF +
          "Content-Length: 5" + CRLF +
          // Content-Type is parsed on the client-side to create an AttachmentDataSource
          // but it is not provided by the Poller service
          CRLF +
          "test3" + CRLF +
          "--12345\r\n" +
          "Content-Disposition: form-data; name=\"testRequestId2-Record\"" + CRLF +
          "Content-Length: 5" + CRLF +
          // Content-Type is parsed on the client-side to create an AttachmentDataSource
          // but it is not provided by the Poller service
          CRLF +
          "test4" + CRLF +
          "--12345--\r\n";

      HttpHeaders responseHeaders = new HttpHeaders();
      responseHeaders.setContentLength(responseBody.length());

      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.GET))
          .andExpect(header("Accept", "multipart/form-data, application/json"))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andRespond(withStatus(HttpStatus.OK)
              .contentType(MediaType.parseMediaType("multipart/form-data; boundary=12345\n"))
              .headers(responseHeaders)
              .body(responseBody));

      // Make the call through SOAP
      // NOTE: Order of results does not match order of parts in multipart response
      List<HasherWsAsynchronousResult> results = proxy.getAllAsynchronousHashResults();

      // Assert first HasherWsAsynchronousResult
      HasherWsAsynchronousResult result1 = results.get(1);

      assertEquals(resultProps1.get("requestId"), result1.getRequestId());
      assertEquals(resultProps1.get("startTime"), result1.getStartTime());
      assertSameBytes(new ByteArrayInputStream((byte[])resultProps1.get("hashResult")),
          new ByteArrayInputStream(result1.getHashResult()));
      assertEquals(resultProps1.get("errorMessage"), result1.getErrorMessage());
      assertEquals(resultProps1.get("status"), result1.getStatus());
      assertEquals(resultProps1.get("bytesHashed"), result1.getBytesHashed());
      assertEquals(resultProps1.get("filesHashed"), result1.getFilesHashed());
      assertEquals(resultProps1.get("elapsedTime"), result1.getElapsedTime());

      // Assert "block" and "record" file names and contents
      assertEquals(resultProps1.get("blockFileName"), result1.getBlockFileName());
      assertEquals(resultProps1.get("recordFileName"), result1.getRecordFileName());
      assertInputStreamMatchesString("test1", result1.getBlockFileDataHandler().getInputStream());
      assertInputStreamMatchesString("test2", result1.getRecordFileDataHandler().getInputStream());

      // Assert other HasherWsAsynchronousResult
      HasherWsAsynchronousResult result2 = results.get(0);

      assertEquals(resultProps2.get("requestId"), result2.getRequestId());
      assertEquals(resultProps2.get("startTime"), result2.getStartTime());
      assertSameBytes(new ByteArrayInputStream((byte[])resultProps2.get("hashResult")),
          new ByteArrayInputStream(result2.getHashResult()));
      assertEquals(resultProps2.get("errorMessage"), result2.getErrorMessage());
      assertEquals(resultProps2.get("status"), result2.getStatus());
      assertEquals(resultProps2.get("bytesHashed"), result2.getBytesHashed());
      assertEquals(resultProps2.get("filesHashed"), result2.getFilesHashed());
      assertEquals(resultProps2.get("elapsedTime"), result2.getElapsedTime());

      // Assert "block" and "record" file names and contents
      assertEquals(resultProps2.get("blockFileName"), result2.getBlockFileName());
      assertEquals(resultProps2.get("recordFileName"), result2.getRecordFileName());
      assertInputStreamMatchesString("test3", result2.getBlockFileDataHandler().getInputStream());;
      assertInputStreamMatchesString("test4", result2.getRecordFileDataHandler().getInputStream());;

      mockRestServer.verify();
      mockRestServer.reset();
    }
  }

  /**
   * Test for {@link HasherService#removeAsynchronousHashRequest(String)}.
   */
  @Test
  public void testRemoveAsynchronousHashRequest() throws Exception {
    //// Test null requestId error ("400 Bad Request") handling
    {
      String requestId = "";

      // REST API endpoint of operation we're testing
      URI restEndpoint =
          RestUtil.getRestUri(getServiceEndpoint(ServiceDescr.SVC_POLLER) + "/ws/hashes/requests/" + requestId, null,
              null);

      String message = "Must supply request identifier";

      HttpHeaders responseHeaders = new HttpHeaders();
      responseHeaders.setContentType(MediaType.TEXT_PLAIN);

      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.DELETE))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andRespond(withStatus(HttpStatus.BAD_REQUEST)
              .headers(responseHeaders)
              .body(message));

      // Make the call through SOAP
      HasherWsAsynchronousResult result = proxy.removeAsynchronousHashRequest(requestId);

      // Assert requestId and status
      assertEquals(requestId, result.getRequestId());
      assertEquals(REQUEST_ERROR, result.getStatus());
      assertEquals(message, result.getErrorMessage());

      mockRestServer.verify();
      mockRestServer.reset();
    }

    //// Test bad or no auth error ("401 Unauthorized") handling
    {
      String requestId = "requestId";

      // REST API endpoint of operation we're testing
      URI restEndpoint =
          RestUtil.getRestUri(getServiceEndpoint(ServiceDescr.SVC_POLLER) + "/ws/hashes/requests/" + requestId, null,
              null);

      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.DELETE))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andRespond(withStatus(HttpStatus.UNAUTHORIZED)
              .contentType(MediaType.TEXT_PLAIN)); // Suppress JSON parsing of empty body

      // Make the call through SOAP
      assertThrows(LockssWebServicesFault.class,
          () -> proxy.removeAsynchronousHashRequest(requestId),
          "401 Unauthorized");

      mockRestServer.verify();
      mockRestServer.reset();
    }

    //// Test bad User Role error ("Forbidden") handling
    {
      String requestId = "requestId";

      // REST API endpoint of operation we're testing
      URI restEndpoint =
          RestUtil.getRestUri(getServiceEndpoint(ServiceDescr.SVC_POLLER) + "/ws/hashes/requests/" + requestId, null,
              null);

      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.DELETE))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andRespond(withStatus(HttpStatus.FORBIDDEN)
              .contentType(MediaType.TEXT_PLAIN)); // Suppress JSON parsing of empty body

      // Make the call through SOAP
      assertThrows(LockssWebServicesFault.class,
          () -> proxy.removeAsynchronousHashRequest(requestId),
          "403 Forbidden");

      mockRestServer.verify();
      mockRestServer.reset();
    }

    //// Test missing request error ("404 Not Found") handling
    {
      String requestId = "requestId";

      // REST API endpoint of operation we're testing
      URI restEndpoint =
          RestUtil.getRestUri(getServiceEndpoint(ServiceDescr.SVC_POLLER) + "/ws/hashes/requests/" + requestId, null,
              null);

      String message = "Cannot find asynchronous hash request '" + requestId + "'";

      HttpHeaders responseHeaders = new HttpHeaders();
      responseHeaders.setContentType(MediaType.TEXT_PLAIN);

      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.DELETE))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andRespond(withStatus(HttpStatus.NOT_FOUND)
              .headers(responseHeaders)
              .body(message));

      // Make the call through SOAP
      HasherWsAsynchronousResult result = proxy.removeAsynchronousHashRequest(requestId);

      // Assert requestId and status
      assertEquals(requestId, result.getRequestId());
      assertEquals(REQUEST_ERROR, result.getStatus());
      assertEquals(message, result.getErrorMessage());

      mockRestServer.verify();
      mockRestServer.reset();
    }

    //// Test unforeseen exception ("500 Internal Server Error") handling
    {
      String requestId = "requestId";

      // REST API endpoint of operation we're testing
      URI restEndpoint =
          RestUtil.getRestUri(getServiceEndpoint(ServiceDescr.SVC_POLLER) + "/ws/hashes/requests/" + requestId, null,
              null);

      String message = "Cannot deleteHash() for requestId = '" + requestId + "'";

      HttpHeaders responseHeaders = new HttpHeaders();
      responseHeaders.setContentType(MediaType.TEXT_PLAIN);

      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.DELETE))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
              .headers(responseHeaders)
              .body(message));

      // Make the call through SOAP
      HasherWsAsynchronousResult result = proxy.removeAsynchronousHashRequest(requestId);

      // Assert requestId and status
      assertEquals(requestId, result.getRequestId());
      assertEquals(REQUEST_ERROR, result.getStatus());
      assertEquals(message, result.getErrorMessage());

      mockRestServer.verify();
      mockRestServer.reset();
    }

    //// Test success
    {
      String requestId = "requestId";

      // REST API endpoint of operation we're testing
      URI restEndpoint =
          RestUtil.getRestUri(getServiceEndpoint(ServiceDescr.SVC_POLLER) + "/ws/hashes/requests/" + requestId, null,
              null);

      String message = "Done";

      HttpHeaders responseHeaders = new HttpHeaders();
      responseHeaders.setContentType(MediaType.TEXT_PLAIN);

      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.DELETE))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andRespond(withStatus(HttpStatus.OK)
              .headers(responseHeaders)
              .body(message));

      // Make the call through SOAP
      HasherWsAsynchronousResult result = proxy.removeAsynchronousHashRequest(requestId);

      // Assert requestId and status
      assertEquals(requestId, result.getRequestId());
      assertEquals(message, result.getStatus());

      mockRestServer.verify();
      mockRestServer.reset();
    }
  }
}
