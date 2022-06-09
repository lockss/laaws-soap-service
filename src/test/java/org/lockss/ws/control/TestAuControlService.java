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
package org.lockss.ws.control;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lockss.log.L4JLogger;
import org.lockss.spring.test.SpringLockssTestCase4;
import org.lockss.util.ListUtil;
import org.lockss.util.rest.RestUtil;
import org.lockss.util.rest.mdx.MetadataUpdateSpec;
import org.lockss.util.rest.poller.PollDesc;
import org.lockss.ws.entities.CheckSubstanceResult;
import org.lockss.ws.entities.RequestAuControlResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lockss.ws.BaseServiceImpl.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"security.basic.enabled=false"})
public class TestAuControlService extends SpringLockssTestCase4 {
  private static final L4JLogger log = L4JLogger.getLogger();

  @TestConfiguration
  public static class MyConfiguration {
    @Bean
    public RestTemplate restTemplate() {
      return new RestTemplate();
    }
  }

  @Autowired
  protected Environment env;

  @Autowired
  private RestTemplate restTemplate;

  @LocalServerPort
  private int port;

  private AuControlService proxy;
  private MockRestServiceServer mockRestServer;

  private static final ObjectMapper mapper = new ObjectMapper();

  private static final String TARGET_NAMESPACE = "http://control.ws.lockss.org/";
  private static final String SERVICE_NAME = "AuControlServiceImplService";

  private static final String USERNAME = "lockss-u";
  private static final String PASSWORD = "lockss-p";
  private static final String BASIC_AUTH_HASH = "Basic bG9ja3NzLXU6bG9ja3NzLXA=";

  @Before
  public void init() throws MalformedURLException {
    // Setup proxy to SOAP service
    String wsdlEndpoint = "http://localhost:" + port + "/ws/AuControlService?wsdl";
    Service srv = Service.create(new URL(wsdlEndpoint), new QName(TARGET_NAMESPACE, SERVICE_NAME));
    proxy = srv.getPort(AuControlService.class);

    // Add authentication headers for SOAP request
    BindingProvider bp = (BindingProvider) proxy;
    Map<String, Object> requestContext = bp.getRequestContext();
    requestContext.put(BindingProvider.USERNAME_PROPERTY, USERNAME);
    requestContext.put(BindingProvider.PASSWORD_PROPERTY, PASSWORD);

    // Create MockRestServiceServer from RestTemplate
    mockRestServer = MockRestServiceServer.createServer(restTemplate);
  }

  /**
   * Test for {@link AuControlService#checkSubstanceById(String)}.
   */
  @Test
  public void testCheckSubstanceById() throws Exception {
    String auId = "testAuid";

    CheckSubstanceResult expectedResult = new CheckSubstanceResult();
    expectedResult.setId(auId);
    expectedResult.setOldState(CheckSubstanceResult.State.No);
    expectedResult.setNewState(CheckSubstanceResult.State.Yes);

    // Prepare the URI path variables
    Map<String, String> uriVariables = new HashMap<>();
    uriVariables.put("auid", auId);
    log.trace("uriVariables = {}", uriVariables);

    // Prepare the endpoint URI
    String checkSubstanceEndpoint = env.getProperty(CONFIG_SVC_URL_KEY) + "/ausubstances/{auid}";
    URI checkSubstanceQuery = RestUtil.getRestUri(checkSubstanceEndpoint, uriVariables, null);

    // Mock REST call for ArtifactData
    mockRestServer
        .expect(ExpectedCount.once(), requestTo(checkSubstanceQuery))
        .andExpect(method(HttpMethod.PUT))
        .andExpect(header("Authorization", BASIC_AUTH_HASH))
        .andRespond(withStatus(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(mapper.writeValueAsString(expectedResult)));

    CheckSubstanceResult result = proxy.checkSubstanceById(auId);

    assertEquals(expectedResult, result);

    mockRestServer.verify();
    mockRestServer.reset();
  }

  private void assertEquals(CheckSubstanceResult expected, CheckSubstanceResult actual) {
    assertEquals(expected.getId(), actual.getId());
    assertEquals(expected.getOldState(), actual.getOldState());
    assertEquals(expected.getNewState(), actual.getNewState());
    assertEquals(expected.getErrorMessage(), actual.getErrorMessage());
  }

  /**
   * Test for {@link AuControlService#checkSubstanceByIdList(List)}.
   */
  @Test
  public void testCheckSubstanceByIdList() throws Exception {
    List<String> auids = ListUtil.list("A", "B", "C");
    List<CheckSubstanceResult> expectedResults = new ArrayList<>(auids.size());

    for (String auId : auids) {
      CheckSubstanceResult result =
          new CheckSubstanceResult(auId, CheckSubstanceResult.State.No, CheckSubstanceResult.State.Yes, null);

      expectedResults.add(result);

      // Prepare the URI path variables
      Map<String, String> uriVariables = new HashMap<>();
      uriVariables.put("auid", auId);
      log.trace("uriVariables = {}", uriVariables);

      // Prepare the endpoint URI
      String checkSubstanceEndpoint = env.getProperty(CONFIG_SVC_URL_KEY) + "/ausubstances/{auid}";
      URI checkSubstanceQuery = RestUtil.getRestUri(checkSubstanceEndpoint, uriVariables, null);

      // Mock REST call for ArtifactData
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(checkSubstanceQuery))
          .andExpect(method(HttpMethod.PUT))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andRespond(withStatus(HttpStatus.OK)
              .contentType(MediaType.APPLICATION_JSON)
              .body(mapper.writeValueAsString(result)));
    }

    // Make SOAP call
    List<CheckSubstanceResult> actualResults = proxy.checkSubstanceByIdList(auids);

    // Assert results match expected
    assertEquals(expectedResults.size(), actualResults.size());
    for (int i = 0; i < auids.size(); i++) {
      assertEquals(expectedResults.get(i), actualResults.get(i));
    }

    mockRestServer.verify();
    mockRestServer.reset();
  }

  /**
   * Test for {@link AuControlService#requestCrawlById(String, Integer, boolean)}.
   */
  @Test
  public void testRequestCrawlById() throws Exception {
    // TODO: Not implemented
  }

  /**
   * Test for {@link AuControlService#requestCrawlByIdList(List, Integer, boolean)}.
   */
  @Test
  public void testRequestCrawlByIdList() throws Exception {
    // TODO: Not implemented
  }

  /**
   * Test for {@link AuControlService#requestDeepCrawlById(String, int, Integer, boolean)}.
   */
  @Test
  public void testRequestDeepCrawlById() throws Exception {
    // TODO: Not implemented
  }

  /**
   * Test for {@link AuControlService#requestDeepCrawlByIdList(List, int, Integer, boolean)}.
   */
  @Test
  public void testRequestDeepCrawlByIdList() throws Exception {
    // TODO: Not implemented
  }

  /**
   * Test for {@link AuControlService#requestPollById(String)}.
   */
  @Test
  public void testRequestPollById() throws Exception {
    String auId = "test";

    PollDesc pollDescription = new PollDesc();
    pollDescription.setAuId(auId);

    // Prepare the endpoint URI
    String requestPollEndpoint = env.getProperty(POLLER_SVC_URL_KEY) + "/polls";
    URI requestPollQuery = RestUtil.getRestUri(requestPollEndpoint, null, null);

    // Mock REST call for ArtifactData
    mockRestServer
        .expect(ExpectedCount.once(), requestTo(requestPollQuery))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header("Authorization", BASIC_AUTH_HASH))
        .andExpect(content().string(mapper.writeValueAsString(pollDescription)))
        .andRespond(withStatus(HttpStatus.OK)
            .body(auId));

    // Make SOAP call
    RequestAuControlResult result = proxy.requestPollById(auId);

    assertEquals(auId, result.getId());
    assertTrue(result.isSuccess());
    assertNull(result.getErrorMessage());

    mockRestServer.verify();
    mockRestServer.reset();
  }

  /**
   * Test for {@link AuControlService#requestPollByIdList(List)}.
   */
  @Test
  public void testRequestPollByIdList() throws Exception {
    List<String> auids = ListUtil.list("A", "B", "C");
    List<RequestAuControlResult> expectedResults = new ArrayList<>(auids.size());

    // Prepare the endpoint URI
    String requestPollEndpoint = env.getProperty(POLLER_SVC_URL_KEY) + "/polls";
    URI requestPollQuery = RestUtil.getRestUri(requestPollEndpoint, null, null);

    for (String auId : auids) {
      RequestAuControlResult result =
          new RequestAuControlResult(auId, true, null);

      expectedResults.add(result);

      PollDesc pollDescription = new PollDesc();
      pollDescription.setAuId(auId);

      // Mock REST call for ArtifactData
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(requestPollQuery))
          .andExpect(method(HttpMethod.POST))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andExpect(content().string(mapper.writeValueAsString(pollDescription)))
          .andRespond(withStatus(HttpStatus.OK)
              .body(auId));
    }

    // Make SOAP call
    List<RequestAuControlResult> actualResults = proxy.requestPollByIdList(auids);

    // Assert results match expected
    assertEquals(expectedResults.size(), actualResults.size());
    for (int i = 0; i < auids.size(); i++) {
      assertEquals(expectedResults.get(i), actualResults.get(i));
    }

    mockRestServer.verify();
    mockRestServer.reset();
  }

  private void assertEquals(RequestAuControlResult expected, RequestAuControlResult actual) {
    assertEquals(expected.getId(), actual.getId());
    assertEquals(expected.isSuccess(), actual.isSuccess());
    assertEquals(expected.getErrorMessage(), actual.getErrorMessage());
  }

  /**
   * Test for {@link AuControlService#requestMdIndexingById(String, boolean)}.
   */
  @Test
  public void testRequestMdIndexingById() throws Exception {
    String auId = "test";
    boolean force = false;

    MetadataUpdateSpec metadataUpdateSpec = new MetadataUpdateSpec();
    metadataUpdateSpec.setAuid(auId);
    metadataUpdateSpec.setUpdateType("full_extraction");

    // Prepare the query parameters
    Map<String, String> queryParams = new HashMap<>(1);
    queryParams.put("force", String.valueOf(force));

    // Prepare the endpoint URI
    String requestMdIndexingEndpoint = env.getProperty(MDX_SVC_URL_KEY) + "/mdupdates";
    URI requestMdIndexingQuery = RestUtil.getRestUri(requestMdIndexingEndpoint, null, queryParams);

    // Mock REST call for ArtifactData
    mockRestServer
        .expect(ExpectedCount.once(), requestTo(requestMdIndexingQuery))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header("Authorization", BASIC_AUTH_HASH))
        .andExpect(content().string(mapper.writeValueAsString(metadataUpdateSpec)))
        .andRespond(withStatus(HttpStatus.OK)
            .body(auId));

    // Make SOAP call
    RequestAuControlResult result = proxy.requestMdIndexingById(auId, force);

    assertEquals(auId, result.getId());
    assertTrue(result.isSuccess());
    assertNull(result.getErrorMessage());

    mockRestServer.verify();
    mockRestServer.reset();
  }

  /**
   * Test for {@link AuControlService#requestMdIndexingByIdList(List, boolean)}.
   */
  @Test
  public void testRequestMdIndexingByIdList() throws Exception {
    List<String> auids = ListUtil.list("A", "B", "C");
    boolean force = false;

    List<RequestAuControlResult> expectedResults = new ArrayList<>(auids.size());

    for (String auId : auids) {
      RequestAuControlResult result =
          new RequestAuControlResult(auId, true, null);

      expectedResults.add(result);

      MetadataUpdateSpec metadataUpdateSpec = new MetadataUpdateSpec();
      metadataUpdateSpec.setAuid(auId);
      metadataUpdateSpec.setUpdateType("full_extraction");

      // Prepare the query parameters
      Map<String, String> queryParams = new HashMap<>(1);
      queryParams.put("force", String.valueOf(force));

      // Prepare the endpoint URI
      String requestMdIndexingEndpoint = env.getProperty(MDX_SVC_URL_KEY) + "/mdupdates";
      URI requestMdIndexingQuery = RestUtil.getRestUri(requestMdIndexingEndpoint, null, queryParams);

      // Mock REST call for ArtifactData
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(requestMdIndexingQuery))
          .andExpect(method(HttpMethod.POST))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andExpect(content().string(mapper.writeValueAsString(metadataUpdateSpec)))
          .andRespond(withStatus(HttpStatus.OK)
              .body(auId));
    }

    // Make SOAP call
    List<RequestAuControlResult> actualResults = proxy.requestMdIndexingByIdList(auids, force);

    // Assert results match expected
    assertEquals(expectedResults.size(), actualResults.size());
    for (int i = 0; i < auids.size(); i++) {
      assertEquals(expectedResults.get(i), actualResults.get(i));
    }

    mockRestServer.verify();
    mockRestServer.reset();
  }

  /**
   * Test for {@link AuControlService#disableMdIndexingById(String)}.
   */
  @Test
  public void testDisableMdIndexingById() throws Exception {
    String auId = "test";
    String auState = "{\"isMetadataExtractionEnabled\":false}";

    Map<String, String> uriVariables = new HashMap<>();
    uriVariables.put("auid", auId);

    // Prepare the endpoint URI
    String requestMdIndexingEndpoint = env.getProperty(CONFIG_SVC_URL_KEY) + "/austates/{auid}";
    URI requestMdIndexingQuery = RestUtil.getRestUri(requestMdIndexingEndpoint, uriVariables, null);

    // Mock REST call for ArtifactData
    mockRestServer
        .expect(ExpectedCount.once(), requestTo(requestMdIndexingQuery))
        .andExpect(method(HttpMethod.PATCH))
        .andExpect(header("Authorization", BASIC_AUTH_HASH))
        .andExpect(request -> assertFalse(request.getHeaders().containsKey("X-Lockss-Request-Cookie")))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().string(auState))
        .andRespond(withStatus(HttpStatus.OK)
            .body(auId));

    // Make SOAP call
    RequestAuControlResult result = proxy.disableMdIndexingById(auId);

    assertEquals(auId, result.getId());
    assertTrue(result.isSuccess());
    assertNull(result.getErrorMessage());

    mockRestServer.verify();
    mockRestServer.reset();
  }

  /**
   * Test for {@link AuControlService#disableMdIndexingByIdList(List)}.
   */
  @Test
  public void testDisableMdIndexingByIdList() throws Exception {
    List<String> auids = ListUtil.list("A", "B", "C");
    List<RequestAuControlResult> expectedResults = new ArrayList<>(auids.size());
    String auState = "{\"isMetadataExtractionEnabled\":false}";

    for (String auId : auids) {
      RequestAuControlResult result =
          new RequestAuControlResult(auId, true, null);

      expectedResults.add(result);

      Map<String, String> uriVariables = new HashMap<>();
      uriVariables.put("auid", auId);

      // Prepare the endpoint URI
      String requestMdIndexingEndpoint = env.getProperty(CONFIG_SVC_URL_KEY) + "/austates/{auid}";
      URI requestMdIndexingQuery = RestUtil.getRestUri(requestMdIndexingEndpoint, uriVariables, null);

      // Mock REST call for ArtifactData
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(requestMdIndexingQuery))
          .andExpect(method(HttpMethod.PATCH))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andExpect(request -> assertFalse(request.getHeaders().containsKey("X-Lockss-Request-Cookie")))
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(content().string(auState))
          .andRespond(withStatus(HttpStatus.OK)
              .body(auId));
    }

    // Make SOAP call
    List<RequestAuControlResult> actualResults = proxy.disableMdIndexingByIdList(auids);

    // Assert results match expected
    assertEquals(expectedResults.size(), actualResults.size());
    for (int i = 0; i < auids.size(); i++) {
      assertEquals(expectedResults.get(i), actualResults.get(i));
    }

    mockRestServer.verify();
    mockRestServer.reset();
  }

  /**
   * Test for {@link AuControlService#enableMdIndexingById(String)}.
   */
  @Test
  public void testEnableMdIndexingById() throws Exception {
    String auId = "test";
    String auState = "{\"isMetadataExtractionEnabled\":true}";

    Map<String, String> uriVariables = new HashMap<>();
    uriVariables.put("auid", auId);

    // Prepare the endpoint URI
    String requestMdIndexingEndpoint = env.getProperty(CONFIG_SVC_URL_KEY) + "/austates/{auid}";
    URI requestMdIndexingQuery = RestUtil.getRestUri(requestMdIndexingEndpoint, uriVariables, null);

    // Mock REST call for ArtifactData
    mockRestServer
        .expect(ExpectedCount.once(), requestTo(requestMdIndexingQuery))
        .andExpect(method(HttpMethod.PATCH))
        .andExpect(header("Authorization", BASIC_AUTH_HASH))
        .andExpect(request -> assertFalse(request.getHeaders().containsKey("X-Lockss-Request-Cookie")))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().string(auState))
        .andRespond(withStatus(HttpStatus.OK)
            .body(auId));

    // Make SOAP call
    RequestAuControlResult result = proxy.enableMdIndexingById(auId);

    assertEquals(auId, result.getId());
    assertTrue(result.isSuccess());
    assertNull(result.getErrorMessage());

    mockRestServer.verify();
    mockRestServer.reset();
  }

  /**
   * Test for {@link AuControlService#enableMdIndexingByIdList(List)}.
   */
  @Test
  public void testEnableMdIndexingByIdList() throws Exception {
    List<String> auids = ListUtil.list("A", "B", "C");
    List<RequestAuControlResult> expectedResults = new ArrayList<>(auids.size());
    String auState = "{\"isMetadataExtractionEnabled\":true}";

    for (String auId : auids) {
      RequestAuControlResult result =
          new RequestAuControlResult(auId, true, null);

      expectedResults.add(result);

      Map<String, String> uriVariables = new HashMap<>();
      uriVariables.put("auid", auId);

      // Prepare the endpoint URI
      String requestMdIndexingEndpoint = env.getProperty(CONFIG_SVC_URL_KEY) + "/austates/{auid}";
      URI requestMdIndexingQuery = RestUtil.getRestUri(requestMdIndexingEndpoint, uriVariables, null);

      // Mock REST call for ArtifactData
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(requestMdIndexingQuery))
          .andExpect(method(HttpMethod.PATCH))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andExpect(request -> assertFalse(request.getHeaders().containsKey("X-Lockss-Request-Cookie")))
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(content().string(auState))
          .andRespond(withStatus(HttpStatus.OK)
              .body(auId));
    }

    // Make SOAP call
    List<RequestAuControlResult> actualResults = proxy.enableMdIndexingByIdList(auids);

    // Assert results match expected
    assertEquals(expectedResults.size(), actualResults.size());
    for (int i = 0; i < auids.size(); i++) {
      assertEquals(expectedResults.get(i), actualResults.get(i));
    }

    mockRestServer.verify();
    mockRestServer.reset();
  }
}
