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
package org.lockss.ws.content;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lockss.app.ServiceDescr;
import org.lockss.log.L4JLogger;
import org.lockss.util.ListUtil;
import org.lockss.util.rest.RestResponseErrorBody;
import org.lockss.ws.entities.ContentConfigurationResult;
import org.lockss.ws.entities.LockssWebServicesFault;
import org.lockss.ws.test.BaseSoapTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.ExpectedCount;

import java.net.URI;
import java.util.List;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"security.basic.enabled=false"})
public class TestContentConfigurationService extends BaseSoapTest {
  private static final L4JLogger log = L4JLogger.getLogger();

  private static final String TARGET_NAMESPACE = "http://content.ws.lockss.org/";
  private static final String SERVICE_NAME = "ContentConfigurationServiceImplService";
  private static final String ENDPOINT_NAME = "ContentConfigurationService";

  private ContentConfigurationService proxy;

  // FIXME: Blank mock REST error response
  private static final RestResponseErrorBody.RestResponseError blankError =
      new RestResponseErrorBody.RestResponseError();

  @Before
  public void init() throws Exception {
    proxy = setUpProxyAndCommonTestEnv(TARGET_NAMESPACE,
                                       ENDPOINT_NAME, SERVICE_NAME,
                                       ContentConfigurationService.class);
  }

  /**
   * Test for {@link ContentConfigurationService#addAuById(String)}.
   */
  @Test
  public void testAddAuById() throws Exception {
    // REST API endpoint of operation we're testing
    URI restEndpoint = new URI(getServiceEndpoint(ServiceDescr.SVC_CONFIG) + "/aus/add");

    List<String> auids = ListUtil.list("auid1");

    //// Test bad or no auth error ("401 Unauthorized") handling
    {
      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.POST))
          .andExpect(content().contentType("application/json"))
          .andExpect(content().string(mapper.writeValueAsString(auids)))
          .andRespond(withStatus(HttpStatus.UNAUTHORIZED)
              .contentType(MediaType.APPLICATION_JSON)
              .body(mapper.writeValueAsString(blankError)));

      // Make the call through SOAP
      assertThrows(LockssWebServicesFault.class,
          () -> proxy.addAuById("auid1"),
          "401 Unauthorized");

      mockRestServer.verify();
      mockRestServer.reset();
    }

    //// Test bad User Role error ("403 Forbidden") handling
    {
      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.POST))
          .andExpect(content().contentType("application/json"))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andExpect(content().string(mapper.writeValueAsString(auids)))
          .andRespond(withStatus(HttpStatus.FORBIDDEN)
              .contentType(MediaType.APPLICATION_JSON)
              .body(mapper.writeValueAsString(blankError)));

      // Make the call through SOAP
      assertThrows(LockssWebServicesFault.class,
          () -> proxy.addAuById("auid1"),
          "403 Forbidden");

      mockRestServer.verify();
      mockRestServer.reset();
    }

    //// Test unforeseen exception ("500 Internal Server Error") handling
    {
      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.POST))
          .andExpect(content().contentType("application/json"))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andExpect(content().string(mapper.writeValueAsString(auids)))
          .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
              .contentType(MediaType.APPLICATION_JSON)
              .body(mapper.writeValueAsString(blankError)));

      // Make the call through SOAP
      assertThrows(LockssWebServicesFault.class,
          () -> proxy.addAuById("auid1"),
          "500 Internal Server Error");

      mockRestServer.verify();
      mockRestServer.reset();
    }

    //// Test "AU successfully added (i.e., configured)"
    {
      // Mock REST response object
      ContentConfigurationResult expectedResult = new ContentConfigurationResult();
      expectedResult.setId("auid1");
      expectedResult.setIsSuccess(true);

      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.POST))
          .andExpect(content().contentType("application/json"))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andExpect(content().string(mapper.writeValueAsString(auids)))
          .andRespond(withStatus(HttpStatus.OK)
              .contentType(MediaType.APPLICATION_JSON)
              .body(mapper.writeValueAsString(ListUtil.list(expectedResult))));

      // Make the call through SOAP
      ContentConfigurationResult result = proxy.addAuById("auid1");
      assertEqualsContentConfigurationResult(expectedResult, result);

      mockRestServer.verify();
      mockRestServer.reset();
    }

    //// Test "AU successfully added (i.e., configured)"
    {
      // Mock REST response object
      ContentConfigurationResult expectedResult = new ContentConfigurationResult();
      expectedResult.setId("auid1");
      expectedResult.setIsSuccess(false);
      expectedResult.setMessage("bad config value");

      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.POST))
          .andExpect(content().contentType("application/json"))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andExpect(content().string(mapper.writeValueAsString(auids)))
          .andRespond(withStatus(HttpStatus.OK)
              .contentType(MediaType.APPLICATION_JSON)
              .body(mapper.writeValueAsString(ListUtil.list(expectedResult))));

      // Make the call through SOAP
      ContentConfigurationResult result = proxy.addAuById("auid1");
      assertEqualsContentConfigurationResult(expectedResult, result);

      mockRestServer.verify();
      mockRestServer.reset();
    }
  }

  /**
   * Test for {@link ContentConfigurationService#addAusByIdList(java.util.List)}.
   */
  @Test
  public void testAddAusByIdList() throws Exception {
    // REST API endpoint of operation we're testing
    URI restEndpoint = new URI(getServiceEndpoint(ServiceDescr.SVC_CONFIG) + "/aus/add");

    //// Test bad or no auth error ("Unauthorized") handling
    {
      List<String> auids = ListUtil.list("auid1", "auid2");

      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.POST))
          .andExpect(content().contentType("application/json"))
          .andExpect(content().string(mapper.writeValueAsString(auids)))
          .andRespond(withStatus(HttpStatus.UNAUTHORIZED)
              .contentType(MediaType.APPLICATION_JSON)
              .body(mapper.writeValueAsString(blankError)));

      // Make the call through SOAP
      assertThrows(LockssWebServicesFault.class,
          () -> proxy.addAusByIdList(auids),
          "401 Unauthorized");

      mockRestServer.verify();
      mockRestServer.reset();
    }

    //// Test bad User Role error ("Forbidden") handling
    {
      List<String> auids = ListUtil.list("auid1", "auid2");

      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.POST))
          .andExpect(content().contentType("application/json"))
          .andExpect(content().string(mapper.writeValueAsString(auids)))
          .andRespond(withStatus(HttpStatus.FORBIDDEN)
              .contentType(MediaType.APPLICATION_JSON)
              .body(mapper.writeValueAsString(blankError)));

      // Make the call through SOAP
      assertThrows(LockssWebServicesFault.class,
          () -> proxy.addAusByIdList(auids),
          "403 Forbidden");

      mockRestServer.verify();
      mockRestServer.reset();
    }

    //// Test unforeseen exception ("500 Internal Server Error") handling
    {
      List<String> auids = ListUtil.list("auid1", "auid2");

      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.POST))
          .andExpect(content().contentType("application/json"))
          .andExpect(content().string(mapper.writeValueAsString(auids)))
          .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
              .contentType(MediaType.APPLICATION_JSON)
              .body(mapper.writeValueAsString(blankError)));

      // Make the call through SOAP
      assertThrows(LockssWebServicesFault.class,
          () -> proxy.addAusByIdList(auids),
          "500 Internal Server Error");

      mockRestServer.verify();
      mockRestServer.reset();
    }

    //// Test successful addition of AUs
    {
      // Mock REST response object
      ContentConfigurationResult expected1 = new ContentConfigurationResult();
      expected1.setId("auid1");
      expected1.setIsSuccess(true);

      ContentConfigurationResult expected2 = new ContentConfigurationResult();
      expected2.setId("auid2");
      expected2.setIsSuccess(true);

      ContentConfigurationResult expected3 = new ContentConfigurationResult();
      expected3.setId("auid3");
      expected3.setIsSuccess(true);

      List<String> auids = ListUtil.list("auid1", "auid2", "auid3");

      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.POST))
          .andExpect(content().contentType("application/json"))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andExpect(content().string(mapper.writeValueAsString(auids)))
          .andRespond(withStatus(HttpStatus.OK)
              .contentType(MediaType.APPLICATION_JSON)
              .body(mapper.writeValueAsString(ListUtil.list(expected1, expected2, expected3))));

      // Make the call through SOAP
      List<ContentConfigurationResult> results = proxy.addAusByIdList(auids);

      ContentConfigurationResult result1 = results.get(0);
      assertEqualsContentConfigurationResult(expected1, result1);

      ContentConfigurationResult result2 = results.get(1);
      assertEqualsContentConfigurationResult(expected2, result2);

      ContentConfigurationResult result3 = results.get(2);
      assertEqualsContentConfigurationResult(expected3, result3);

      mockRestServer.verify();
      mockRestServer.reset();
    }

    //// Test mixed success
    {
      // Mock REST response object
      ContentConfigurationResult expected1 = new ContentConfigurationResult();
      expected1.setId("auid1");
      expected1.setIsSuccess(true);

      ContentConfigurationResult expected2 = new ContentConfigurationResult();
      expected2.setId("auid2");
      expected2.setIsSuccess(false);

      List<String> auids = ListUtil.list("auid1", "auid2");

      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.POST))
          .andExpect(content().contentType("application/json"))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andExpect(content().string(mapper.writeValueAsString(auids)))
          .andRespond(withStatus(HttpStatus.OK)
              .contentType(MediaType.APPLICATION_JSON)
              .body(mapper.writeValueAsString(ListUtil.list(expected1, expected2))));

      // Make the call through SOAP
      List<ContentConfigurationResult> results = proxy.addAusByIdList(auids);

      ContentConfigurationResult result1 = results.get(0);
      assertEqualsContentConfigurationResult(expected1, result1);

      ContentConfigurationResult result2 = results.get(1);
      assertEqualsContentConfigurationResult(expected2, result2);

      mockRestServer.verify();
      mockRestServer.reset();
    }
  }

  /**
   * Asserts two {@link ContentConfigurationResult} are the same.
   */
  private static void assertEqualsContentConfigurationResult(
      ContentConfigurationResult expected, ContentConfigurationResult actual) {

    assertEquals(expected.getId(), actual.getId());
    assertEquals(expected.getName(), actual.getName());
    assertEquals(expected.getIsSuccess(), actual.getIsSuccess());
    assertEquals(expected.getMessage(), actual.getMessage());
  }

  /**
   * Test for {@link ContentConfigurationService#deleteAuById(String)}.
   */
  @Test
  public void testDeleteAuById() throws Exception {
    // REST API endpoint of operation we're testing
    URI restEndpoint = new URI(getServiceEndpoint(ServiceDescr.SVC_CONFIG) + "/aus/delete");

    List<String> auids = ListUtil.list("auid1");

    //// Test bad or no auth error ("401 Unauthorized") handling
    {
      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.DELETE))
          .andExpect(content().contentType("application/json"))
          .andExpect(content().string(mapper.writeValueAsString(auids)))
          .andRespond(withStatus(HttpStatus.UNAUTHORIZED)
              .contentType(MediaType.APPLICATION_JSON)
              .body(mapper.writeValueAsString(blankError)));

      // Make the call through SOAP
      assertThrows(LockssWebServicesFault.class,
          () -> proxy.deleteAuById("auid1"),
          "401 Unauthorized");

      mockRestServer.verify();
      mockRestServer.reset();
    }

    //// Test bad User Role error ("403 Forbidden") handling
    {
      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.DELETE))
          .andExpect(content().contentType("application/json"))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andExpect(content().string(mapper.writeValueAsString(auids)))
          .andRespond(withStatus(HttpStatus.FORBIDDEN)
              .contentType(MediaType.APPLICATION_JSON)
              .body(mapper.writeValueAsString(blankError)));

      // Make the call through SOAP
      assertThrows(LockssWebServicesFault.class,
          () -> proxy.deleteAuById("auid1"),
          "403 Forbidden");

      mockRestServer.verify();
      mockRestServer.reset();
    }

    //// Test unforeseen exception ("500 Internal Server Error") handling
    {
      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.DELETE))
          .andExpect(content().contentType("application/json"))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andExpect(content().string(mapper.writeValueAsString(auids)))
          .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
              .contentType(MediaType.APPLICATION_JSON)
              .body(mapper.writeValueAsString(blankError)));

      // Make the call through SOAP
      assertThrows(LockssWebServicesFault.class,
          () -> proxy.deleteAuById("auid1"),
          "500 Internal Server Error");

      mockRestServer.verify();
      mockRestServer.reset();
    }

    //// Test successful deletion of AU
    {
      // Mock REST response object
      ContentConfigurationResult expectedResult = new ContentConfigurationResult();
      expectedResult.setId("auid1");
      expectedResult.setIsSuccess(true);

      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.DELETE))
          .andExpect(content().contentType("application/json"))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andExpect(content().string(mapper.writeValueAsString(auids)))
          .andRespond(withStatus(HttpStatus.OK)
              .contentType(MediaType.APPLICATION_JSON)
              .body(mapper.writeValueAsString(ListUtil.list(expectedResult))));

      // Make the call through SOAP
      ContentConfigurationResult result = proxy.deleteAuById("auid1");
      assertEqualsContentConfigurationResult(expectedResult, result);

      mockRestServer.verify();
      mockRestServer.reset();
    }

    //// Test failed deletion of AU
    {
      // Mock REST response object
      ContentConfigurationResult expectedResult = new ContentConfigurationResult();
      expectedResult.setId("auid1");
      expectedResult.setIsSuccess(false);

      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.DELETE))
          .andExpect(content().contentType("application/json"))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andExpect(content().string(mapper.writeValueAsString(auids)))
          .andRespond(withStatus(HttpStatus.OK)
              .contentType(MediaType.APPLICATION_JSON)
              .body(mapper.writeValueAsString(ListUtil.list(expectedResult))));

      // Make the call through SOAP
      ContentConfigurationResult result = proxy.deleteAuById("auid1");
      assertEqualsContentConfigurationResult(expectedResult, result);

      mockRestServer.verify();
      mockRestServer.reset();
    }
  }

  /**
   * Test for {@link ContentConfigurationService#deleteAusByIdList(List)}.
   */
  @Test
  public void testDeleteAusByIdList() throws Exception {
    // REST API endpoint of operation we're testing
    URI restEndpoint = new URI(getServiceEndpoint(ServiceDescr.SVC_CONFIG) + "/aus/delete");

    //// Test bad or no auth error ("Unauthorized") handling
    {
      List<String> auids = ListUtil.list("auid1", "auid2");

      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.DELETE))
          .andExpect(content().contentType("application/json"))
          .andExpect(content().string(mapper.writeValueAsString(auids)))
          .andRespond(withStatus(HttpStatus.UNAUTHORIZED)
              .contentType(MediaType.APPLICATION_JSON)
              .body(mapper.writeValueAsString(blankError)));

      // Make the call through SOAP
      assertThrows(LockssWebServicesFault.class,
          () -> proxy.deleteAusByIdList(auids),
          "401 Unauthorized");

      mockRestServer.verify();
      mockRestServer.reset();
    }

    //// Test bad User Role error ("Forbidden") handling
    {
      List<String> auids = ListUtil.list("auid1", "auid2");

      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.DELETE))
          .andExpect(content().contentType("application/json"))
          .andExpect(content().string(mapper.writeValueAsString(auids)))
          .andRespond(withStatus(HttpStatus.FORBIDDEN)
              .contentType(MediaType.APPLICATION_JSON)
              .body(mapper.writeValueAsString(blankError)));

      // Make the call through SOAP
      assertThrows(LockssWebServicesFault.class,
          () -> proxy.deleteAusByIdList(auids),
          "403 Forbidden");

      mockRestServer.verify();
      mockRestServer.reset();
    }

    //// Test unforeseen exception ("500 Internal Server Error") handling
    {
      List<String> auids = ListUtil.list("auid1", "auid2");

      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.DELETE))
          .andExpect(content().contentType("application/json"))
          .andExpect(content().string(mapper.writeValueAsString(auids)))
          .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
              .contentType(MediaType.APPLICATION_JSON)
              .body(mapper.writeValueAsString(blankError)));

      // Make the call through SOAP
      assertThrows(LockssWebServicesFault.class,
          () -> proxy.deleteAusByIdList(auids),
          "500 Internal Server Error");

      mockRestServer.verify();
      mockRestServer.reset();
    }

    //// Test successful addition of AUs
    {
      // Mock REST response object
      ContentConfigurationResult expected1 = new ContentConfigurationResult();
      expected1.setId("auid1");
      expected1.setIsSuccess(true);

      ContentConfigurationResult expected2 = new ContentConfigurationResult();
      expected2.setId("auid2");
      expected2.setIsSuccess(true);

      ContentConfigurationResult expected3 = new ContentConfigurationResult();
      expected3.setId("auid3");
      expected3.setIsSuccess(true);

      List<String> auids = ListUtil.list("auid1", "auid2", "auid3");

      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.DELETE))
          .andExpect(content().contentType("application/json"))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andExpect(content().string(mapper.writeValueAsString(auids)))
          .andRespond(withStatus(HttpStatus.OK)
              .contentType(MediaType.APPLICATION_JSON)
              .body(mapper.writeValueAsString(ListUtil.list(expected1, expected2, expected3))));

      // Make the call through SOAP
      List<ContentConfigurationResult> results = proxy.deleteAusByIdList(auids);

      ContentConfigurationResult result1 = results.get(0);
      assertEqualsContentConfigurationResult(expected1, result1);

      ContentConfigurationResult result2 = results.get(1);
      assertEqualsContentConfigurationResult(expected2, result2);

      ContentConfigurationResult result3 = results.get(2);
      assertEqualsContentConfigurationResult(expected3, result3);

      mockRestServer.verify();
      mockRestServer.reset();
    }

    //// Test mixed success
    {
      // Mock REST response object
      ContentConfigurationResult expected1 = new ContentConfigurationResult();
      expected1.setId("auid1");
      expected1.setIsSuccess(true);

      ContentConfigurationResult expected2 = new ContentConfigurationResult();
      expected2.setId("auid2");
      expected2.setIsSuccess(false);

      List<String> auids = ListUtil.list("auid1", "auid2");

      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.DELETE))
          .andExpect(content().contentType("application/json"))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andExpect(content().string(mapper.writeValueAsString(auids)))
          .andRespond(withStatus(HttpStatus.OK)
              .contentType(MediaType.APPLICATION_JSON)
              .body(mapper.writeValueAsString(ListUtil.list(expected1, expected2))));

      // Make the call through SOAP
      List<ContentConfigurationResult> results = proxy.deleteAusByIdList(auids);

      ContentConfigurationResult result1 = results.get(0);
      assertEqualsContentConfigurationResult(expected1, result1);

      ContentConfigurationResult result2 = results.get(1);
      assertEqualsContentConfigurationResult(expected2, result2);

      mockRestServer.verify();
      mockRestServer.reset();
    }
  }

  /**
   * Test for {@link ContentConfigurationService#reactivateAuById(String)}.
   */
  @Test
  public void testReactivateAuById() throws Exception {
    // REST API endpoint of operation we're testing
    URI restEndpoint = new URI(getServiceEndpoint(ServiceDescr.SVC_CONFIG) + "/aus/reactivate");

    List<String> auids = ListUtil.list("auid1");

    //// Test bad or no auth error ("401 Unauthorized") handling
    {
      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.PUT))
          .andExpect(content().contentType("application/json"))
          .andExpect(content().string(mapper.writeValueAsString(auids)))
          .andRespond(withStatus(HttpStatus.UNAUTHORIZED)
              .contentType(MediaType.APPLICATION_JSON)
              .body(mapper.writeValueAsString(blankError)));

      // Make the call through SOAP
      assertThrows(LockssWebServicesFault.class,
          () -> proxy.reactivateAuById("auid1"),
          "401 Unauthorized");

      mockRestServer.verify();
      mockRestServer.reset();
    }

    //// Test bad User Role error ("403 Forbidden") handling
    {
      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.PUT))
          .andExpect(content().contentType("application/json"))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andExpect(content().string(mapper.writeValueAsString(auids)))
          .andRespond(withStatus(HttpStatus.FORBIDDEN)
              .contentType(MediaType.APPLICATION_JSON)
              .body(mapper.writeValueAsString(blankError)));

      // Make the call through SOAP
      assertThrows(LockssWebServicesFault.class,
          () -> proxy.reactivateAuById("auid1"),
          "403 Forbidden");

      mockRestServer.verify();
      mockRestServer.reset();
    }

    //// Test unforeseen exception ("500 Internal Server Error") handling
    {
      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.PUT))
          .andExpect(content().contentType("application/json"))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andExpect(content().string(mapper.writeValueAsString(auids)))
          .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
              .contentType(MediaType.APPLICATION_JSON)
              .body(mapper.writeValueAsString(blankError)));

      // Make the call through SOAP
      assertThrows(LockssWebServicesFault.class,
          () -> proxy.reactivateAuById("auid1"),
          "500 Internal Server Error");

      mockRestServer.verify();
      mockRestServer.reset();
    }

    //// Test successful reactivation of AU
    {
      // Mock REST response object
      ContentConfigurationResult expectedResult = new ContentConfigurationResult();
      expectedResult.setId("auid1");
      expectedResult.setIsSuccess(true);

      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.PUT))
          .andExpect(content().contentType("application/json"))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andExpect(content().string(mapper.writeValueAsString(auids)))
          .andRespond(withStatus(HttpStatus.OK)
              .contentType(MediaType.APPLICATION_JSON)
              .body(mapper.writeValueAsString(ListUtil.list(expectedResult))));

      // Make the call through SOAP
      ContentConfigurationResult result = proxy.reactivateAuById("auid1");
      assertEqualsContentConfigurationResult(expectedResult, result);

      mockRestServer.verify();
      mockRestServer.reset();
    }

    //// Test failed reactivation of AU
    {
      // Mock REST response object
      ContentConfigurationResult expectedResult = new ContentConfigurationResult();
      expectedResult.setId("auid1");
      expectedResult.setIsSuccess(false);

      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.PUT))
          .andExpect(content().contentType("application/json"))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andExpect(content().string(mapper.writeValueAsString(auids)))
          .andRespond(withStatus(HttpStatus.OK)
              .contentType(MediaType.APPLICATION_JSON)
              .body(mapper.writeValueAsString(ListUtil.list(expectedResult))));

      // Make the call through SOAP
      ContentConfigurationResult result = proxy.reactivateAuById("auid1");
      assertEqualsContentConfigurationResult(expectedResult, result);

      mockRestServer.verify();
      mockRestServer.reset();
    }
  }

  /**
   * Test for {@link ContentConfigurationService#reactivateAusByIdList(List)}.
   */
  @Test
  public void testReactivateAusByIdList() throws Exception {
    // REST API endpoint of operation we're testing
    URI restEndpoint = new URI(getServiceEndpoint(ServiceDescr.SVC_CONFIG) + "/aus/reactivate");

    //// Test bad or no auth error ("Unauthorized") handling
    {
      List<String> auids = ListUtil.list("auid1", "auid2");

      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.PUT))
          .andExpect(content().contentType("application/json"))
          .andExpect(content().string(mapper.writeValueAsString(auids)))
          .andRespond(withStatus(HttpStatus.UNAUTHORIZED)
              .contentType(MediaType.APPLICATION_JSON)
              .body(mapper.writeValueAsString(blankError)));

      // Make the call through SOAP
      assertThrows(LockssWebServicesFault.class,
          () -> proxy.reactivateAusByIdList(auids),
          "401 Unauthorized");

      mockRestServer.verify();
      mockRestServer.reset();
    }

    //// Test bad User Role error ("Forbidden") handling
    {
      List<String> auids = ListUtil.list("auid1", "auid2");

      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.PUT))
          .andExpect(content().contentType("application/json"))
          .andExpect(content().string(mapper.writeValueAsString(auids)))
          .andRespond(withStatus(HttpStatus.FORBIDDEN)
              .contentType(MediaType.APPLICATION_JSON)
              .body(mapper.writeValueAsString(blankError)));

      // Make the call through SOAP
      assertThrows(LockssWebServicesFault.class,
          () -> proxy.reactivateAusByIdList(auids),
          "403 Forbidden");

      mockRestServer.verify();
      mockRestServer.reset();
    }

    //// Test unforeseen exception ("500 Internal Server Error") handling
    {
      List<String> auids = ListUtil.list("auid1", "auid2");

      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.PUT))
          .andExpect(content().contentType("application/json"))
          .andExpect(content().string(mapper.writeValueAsString(auids)))
          .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
              .contentType(MediaType.APPLICATION_JSON)
              .body(mapper.writeValueAsString(blankError)));

      // Make the call through SOAP
      assertThrows(LockssWebServicesFault.class,
          () -> proxy.reactivateAusByIdList(auids),
          "500 Internal Server Error");

      mockRestServer.verify();
      mockRestServer.reset();
    }

    //// Test successful reactivation of AUs
    {
      // Mock REST response object
      ContentConfigurationResult expected1 = new ContentConfigurationResult();
      expected1.setId("auid1");
      expected1.setIsSuccess(true);

      ContentConfigurationResult expected2 = new ContentConfigurationResult();
      expected2.setId("auid2");
      expected2.setIsSuccess(true);

      ContentConfigurationResult expected3 = new ContentConfigurationResult();
      expected3.setId("auid3");
      expected3.setIsSuccess(true);

      List<String> auids = ListUtil.list("auid1", "auid2", "auid3");

      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.PUT))
          .andExpect(content().contentType("application/json"))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andExpect(content().string(mapper.writeValueAsString(auids)))
          .andRespond(withStatus(HttpStatus.OK)
              .contentType(MediaType.APPLICATION_JSON)
              .body(mapper.writeValueAsString(ListUtil.list(expected1, expected2, expected3))));

      // Make the call through SOAP
      List<ContentConfigurationResult> results = proxy.reactivateAusByIdList(auids);

      ContentConfigurationResult result1 = results.get(0);
      assertEqualsContentConfigurationResult(expected1, result1);

      ContentConfigurationResult result2 = results.get(1);
      assertEqualsContentConfigurationResult(expected2, result2);

      ContentConfigurationResult result3 = results.get(2);
      assertEqualsContentConfigurationResult(expected3, result3);

      mockRestServer.verify();
      mockRestServer.reset();
    }

    //// Test mixed success
    {
      // Mock REST response object
      ContentConfigurationResult expected1 = new ContentConfigurationResult();
      expected1.setId("auid1");
      expected1.setIsSuccess(true);

      ContentConfigurationResult expected2 = new ContentConfigurationResult();
      expected2.setId("auid2");
      expected2.setIsSuccess(false);

      List<String> auids = ListUtil.list("auid1", "auid2");

      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.PUT))
          .andExpect(content().contentType("application/json"))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andExpect(content().string(mapper.writeValueAsString(auids)))
          .andRespond(withStatus(HttpStatus.OK)
              .contentType(MediaType.APPLICATION_JSON)
              .body(mapper.writeValueAsString(ListUtil.list(expected1, expected2))));

      // Make the call through SOAP
      List<ContentConfigurationResult> results = proxy.reactivateAusByIdList(auids);

      ContentConfigurationResult result1 = results.get(0);
      assertEqualsContentConfigurationResult(expected1, result1);

      ContentConfigurationResult result2 = results.get(1);
      assertEqualsContentConfigurationResult(expected2, result2);

      mockRestServer.verify();
      mockRestServer.reset();
    }
  }

  /**
   * Test for {@link ContentConfigurationService#deactivateAuById(String)}.
   */
  @Test
  public void testDeactivateAuById() throws Exception {
    // REST API endpoint of operation we're testing
    URI restEndpoint = new URI(getServiceEndpoint(ServiceDescr.SVC_CONFIG) + "/aus/deactivate");

    List<String> auids = ListUtil.list("auid1");

    //// Test bad or no auth error ("401 Unauthorized") handling
    {
      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.PUT))
          .andExpect(content().contentType("application/json"))
          .andExpect(content().string(mapper.writeValueAsString(auids)))
          .andRespond(withStatus(HttpStatus.UNAUTHORIZED)
              .contentType(MediaType.APPLICATION_JSON)
              .body(mapper.writeValueAsString(blankError)));

      // Make the call through SOAP
      assertThrows(LockssWebServicesFault.class,
          () -> proxy.deactivateAuById("auid1"),
          "401 Unauthorized");

      mockRestServer.verify();
      mockRestServer.reset();
    }

    //// Test bad User Role error ("403 Forbidden") handling
    {
      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.PUT))
          .andExpect(content().contentType("application/json"))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andExpect(content().string(mapper.writeValueAsString(auids)))
          .andRespond(withStatus(HttpStatus.FORBIDDEN)
              .contentType(MediaType.APPLICATION_JSON)
              .body(mapper.writeValueAsString(blankError)));

      // Make the call through SOAP
      assertThrows(LockssWebServicesFault.class,
          () -> proxy.deactivateAuById("auid1"),
          "403 Forbidden");

      mockRestServer.verify();
      mockRestServer.reset();
    }

    //// Test unforeseen exception ("500 Internal Server Error") handling
    {
      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.PUT))
          .andExpect(content().contentType("application/json"))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andExpect(content().string(mapper.writeValueAsString(auids)))
          .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
              .contentType(MediaType.APPLICATION_JSON)
              .body(mapper.writeValueAsString(blankError)));

      // Make the call through SOAP
      assertThrows(LockssWebServicesFault.class,
          () -> proxy.deactivateAuById("auid1"),
          "500 Internal Server Error");

      mockRestServer.verify();
      mockRestServer.reset();
    }

    //// Test successful deactivation of AU
    {
      // Mock REST response object
      ContentConfigurationResult expectedResult = new ContentConfigurationResult();
      expectedResult.setId("auid1");
      expectedResult.setIsSuccess(true);

      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.PUT))
          .andExpect(content().contentType("application/json"))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andExpect(content().string(mapper.writeValueAsString(auids)))
          .andRespond(withStatus(HttpStatus.OK)
              .contentType(MediaType.APPLICATION_JSON)
              .body(mapper.writeValueAsString(ListUtil.list(expectedResult))));

      // Make the call through SOAP
      ContentConfigurationResult result = proxy.deactivateAuById("auid1");
      assertEqualsContentConfigurationResult(expectedResult, result);

      mockRestServer.verify();
      mockRestServer.reset();
    }

    //// Test failed deactivation of AU
    {
      // Mock REST response object
      ContentConfigurationResult expectedResult = new ContentConfigurationResult();
      expectedResult.setId("auid1");
      expectedResult.setIsSuccess(false);

      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.PUT))
          .andExpect(content().contentType("application/json"))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andExpect(content().string(mapper.writeValueAsString(auids)))
          .andRespond(withStatus(HttpStatus.OK)
              .contentType(MediaType.APPLICATION_JSON)
              .body(mapper.writeValueAsString(ListUtil.list(expectedResult))));

      // Make the call through SOAP
      ContentConfigurationResult result = proxy.deactivateAuById("auid1");
      assertEqualsContentConfigurationResult(expectedResult, result);

      mockRestServer.verify();
      mockRestServer.reset();
    }
  }

  /**
   * Test for {@link ContentConfigurationService#deactivateAusByIdList(List)}.
   */
  @Test
  public void testDeactivateAusByIdList() throws Exception {
    // REST API endpoint of operation we're testing
    URI restEndpoint = new URI(getServiceEndpoint(ServiceDescr.SVC_CONFIG) + "/aus/deactivate");

    //// Test bad or no auth error ("Unauthorized") handling
    {
      List<String> auids = ListUtil.list("auid1", "auid2");

      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.PUT))
          .andExpect(content().contentType("application/json"))
          .andExpect(content().string(mapper.writeValueAsString(auids)))
          .andRespond(withStatus(HttpStatus.UNAUTHORIZED)
              .contentType(MediaType.APPLICATION_JSON)
              .body(mapper.writeValueAsString(blankError)));

      // Make the call through SOAP
      assertThrows(LockssWebServicesFault.class,
          () -> proxy.deactivateAusByIdList(auids),
          "401 Unauthorized");

      mockRestServer.verify();
      mockRestServer.reset();
    }

    //// Test bad User Role error ("Forbidden") handling
    {
      List<String> auids = ListUtil.list("auid1", "auid2");

      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.PUT))
          .andExpect(content().contentType("application/json"))
          .andExpect(content().string(mapper.writeValueAsString(auids)))
          .andRespond(withStatus(HttpStatus.FORBIDDEN)
              .contentType(MediaType.APPLICATION_JSON)
              .body(mapper.writeValueAsString(blankError)));

      // Make the call through SOAP
      assertThrows(LockssWebServicesFault.class,
          () -> proxy.deactivateAusByIdList(auids),
          "403 Forbidden");

      mockRestServer.verify();
      mockRestServer.reset();
    }

    //// Test unforeseen exception ("500 Internal Server Error") handling
    {
      List<String> auids = ListUtil.list("auid1", "auid2");

      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.PUT))
          .andExpect(content().contentType("application/json"))
          .andExpect(content().string(mapper.writeValueAsString(auids)))
          .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
              .contentType(MediaType.APPLICATION_JSON)
              .body(mapper.writeValueAsString(blankError)));

      // Make the call through SOAP
      assertThrows(LockssWebServicesFault.class,
          () -> proxy.deactivateAusByIdList(auids),
          "500 Internal Server Error");

      mockRestServer.verify();
      mockRestServer.reset();
    }

    //// Test successful deactivation of AUs
    {
      // Mock REST response object
      ContentConfigurationResult expected1 = new ContentConfigurationResult();
      expected1.setId("auid1");
      expected1.setIsSuccess(true);

      ContentConfigurationResult expected2 = new ContentConfigurationResult();
      expected2.setId("auid2");
      expected2.setIsSuccess(true);

      ContentConfigurationResult expected3 = new ContentConfigurationResult();
      expected3.setId("auid3");
      expected3.setIsSuccess(true);

      List<String> auids = ListUtil.list("auid1", "auid2", "auid3");

      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.PUT))
          .andExpect(content().contentType("application/json"))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andExpect(content().string(mapper.writeValueAsString(auids)))
          .andRespond(withStatus(HttpStatus.OK)
              .contentType(MediaType.APPLICATION_JSON)
              .body(mapper.writeValueAsString(ListUtil.list(expected1, expected2, expected3))));

      // Make the call through SOAP
      List<ContentConfigurationResult> results = proxy.deactivateAusByIdList(auids);

      ContentConfigurationResult result1 = results.get(0);
      assertEqualsContentConfigurationResult(expected1, result1);

      ContentConfigurationResult result2 = results.get(1);
      assertEqualsContentConfigurationResult(expected2, result2);

      ContentConfigurationResult result3 = results.get(2);
      assertEqualsContentConfigurationResult(expected3, result3);

      mockRestServer.verify();
      mockRestServer.reset();
    }

    //// Test mixed success
    {
      // Mock REST response object
      ContentConfigurationResult expected1 = new ContentConfigurationResult();
      expected1.setId("auid1");
      expected1.setIsSuccess(true);

      ContentConfigurationResult expected2 = new ContentConfigurationResult();
      expected2.setId("auid2");
      expected2.setIsSuccess(false);

      List<String> auids = ListUtil.list("auid1", "auid2");

      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.PUT))
          .andExpect(content().contentType("application/json"))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andExpect(content().string(mapper.writeValueAsString(auids)))
          .andRespond(withStatus(HttpStatus.OK)
              .contentType(MediaType.APPLICATION_JSON)
              .body(mapper.writeValueAsString(ListUtil.list(expected1, expected2))));

      // Make the call through SOAP
      List<ContentConfigurationResult> results = proxy.deactivateAusByIdList(auids);

      ContentConfigurationResult result1 = results.get(0);
      assertEqualsContentConfigurationResult(expected1, result1);

      ContentConfigurationResult result2 = results.get(1);
      assertEqualsContentConfigurationResult(expected2, result2);

      mockRestServer.verify();
      mockRestServer.reset();
    }
  }
}
