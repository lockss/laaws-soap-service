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
package org.lockss.ws.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jeasy.random.EasyRandom;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lockss.laaws.rs.model.Artifact;
import org.lockss.laaws.rs.model.ArtifactPageInfo;
import org.lockss.laaws.rs.model.PageInfo;
import org.lockss.log.L4JLogger;
import org.lockss.spring.test.SpringLockssTestCase4;
import org.lockss.util.ListUtil;
import org.lockss.util.rest.RestUtil;
import org.lockss.util.rest.status.ApiStatus;
import org.lockss.ws.entities.*;
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
import java.util.*;
import java.util.stream.Collectors;

import static org.lockss.ws.BaseServiceImpl.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"security.basic.enabled=false"})
public class TestDaemonStatusService extends SpringLockssTestCase4 {
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

  private DaemonStatusService proxy;
  private MockRestServiceServer mockRestServer;
  private static EasyRandom easyRandom;

  private static final ObjectMapper mapper = new ObjectMapper();

  private static final String TARGET_NAMESPACE = "http://status.ws.lockss.org/";
  private static final String SERVICE_NAME = "DaemonStatusServiceImplService";

  private static final String USERNAME = "lockss-u";
  private static final String PASSWORD = "lockss-p";
  private static final String BASIC_AUTH_HASH = "Basic bG9ja3NzLXU6bG9ja3NzLXA=";

  @Before
  public void init() throws MalformedURLException {
    // Setup proxy to SOAP service
    String wsdlEndpoint = "http://localhost:" + port + "/ws/DaemonStatusService?wsdl";
    Service srv = Service.create(new URL(wsdlEndpoint), new QName(TARGET_NAMESPACE, SERVICE_NAME));
    proxy = srv.getPort(DaemonStatusService.class);

    // Add authentication headers for SOAP request
    BindingProvider bp = (BindingProvider) proxy;
    Map<String, Object> requestContext = bp.getRequestContext();
    requestContext.put(BindingProvider.USERNAME_PROPERTY, USERNAME);
    requestContext.put(BindingProvider.PASSWORD_PROPERTY, PASSWORD);

    // Create MockRestServiceServer from RestTemplate
    mockRestServer = MockRestServiceServer.createServer(restTemplate);

    // Create EasyRandom generator
    easyRandom = new EasyRandom();
  }

  /**
   * Test for {@link DaemonStatusService#isDaemonReady()}.
   */
  @Test
  public void testIsDaemonReady() throws Exception {
    int allSvcsStopped = 0b00000;
    int allSvcsReady = 0b11111;

    for (int svcsReady = allSvcsStopped; svcsReady <= allSvcsReady; svcsReady++) {

      boolean isRepoReady = (svcsReady & 0b10000) != 0;
      boolean isCfgReady = (svcsReady & 0b01000) != 0;
      boolean isPollerReady = (svcsReady & 0b00100) != 0;
      boolean isMdxReady = (svcsReady & 0b00010) != 0;
      boolean isMdqReady = (svcsReady & 0b00001) != 0;

      boolean isPollerCallExpected = (svcsReady & 0b11000) == 0b11000;
      boolean isMdxCallExpected = (svcsReady & 0b11100) == 0b11100;
      boolean isMdqCallExpected = (svcsReady & 0b11110) == 0b11110;

      // boolean isDaemonReady = isRepoReady & isCfgReady & isPollerReady && isMdxReady & isMdqReady;
      boolean isDaemonReady = svcsReady == allSvcsReady;

      mockIsServiceReady(env.getProperty(REPO_SVC_URL_KEY), isRepoReady, true);
      mockIsServiceReady(env.getProperty(CONFIG_SVC_URL_KEY), isCfgReady, isRepoReady);
      mockIsServiceReady(env.getProperty(POLLER_SVC_URL_KEY), isPollerReady, isPollerCallExpected);
      mockIsServiceReady(env.getProperty(MDX_SVC_URL_KEY), isMdxReady, isMdxCallExpected);
      mockIsServiceReady(env.getProperty(MDQ_SVC_URL_KEY), isMdqReady, isMdqCallExpected);

      assertEquals(isDaemonReady, proxy.isDaemonReady());

      mockRestServer.verify();
      mockRestServer.reset();
    }
  }

  private void mockIsServiceReady(String url, boolean isReady, boolean isCallExpected) throws Exception {
    // Prepare the endpoint URI
    String statusEndpoint = url + "/status";
    URI statusQuery = RestUtil.getRestUri(statusEndpoint, null, null);

    if (!isCallExpected) {
      mockRestServer.expect(ExpectedCount.never(), requestTo(statusQuery));
      return;
    }

    ApiStatus apiStatus = easyRandom.nextObject(ApiStatus.class);
    apiStatus.setReady(isReady);

    // Mock REST call for ArtifactData
    mockRestServer
        .expect(ExpectedCount.once(), requestTo(statusQuery))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withStatus(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(mapper.writeValueAsString(apiStatus)));
  }

  /**
   * Test for {@link DaemonStatusService#getAuIds()}.
   */
  @Test
  public void testGetAuIds() throws Exception {
    // Generate REST response
    List<AuWsResult> restResponse =
        ListUtil.list(easyRandom.nextObject(AuWsResult[].class));

    // Generate expected SOAP response map (AUID -> IdNamePair)
    Map<String, IdNamePair> expectedResultMap = new HashMap<>();

    for (AuWsResult result : restResponse) {
      IdNamePair resultElement = new IdNamePair(result.getAuId(), result.getName());
      expectedResultMap.put(result.getAuId(), resultElement);
    }

    String auQuery = "select auId, name";

    // Prepare the query parameters
    Map<String, String> queryParams = new HashMap<>(1);
    queryParams.put("auQuery", auQuery);

    // Prepare the endpoint URI
    String auQueriesEndpoint = env.getProperty(CONFIG_SVC_URL_KEY) + "/auqueries";
    URI auQueriesQuery = RestUtil.getRestUri(auQueriesEndpoint, null, queryParams);

    mockRestServer
        .expect(ExpectedCount.once(), requestTo(auQueriesQuery))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("Authorization", BASIC_AUTH_HASH))
        .andRespond(withStatus(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(mapper.writeValueAsString(restResponse)));

    // Make SOAP call
    Collection<IdNamePair> result = proxy.getAuIds();

    // Assert expected result
    assertEquals(expectedResultMap.size(), result.size());

    for (IdNamePair actual : result) {
      IdNamePair expected = expectedResultMap.get(actual.getId());

      assertNotNull(expected);
      assertEquals(expected.getId(), actual.getId());
      assertEquals(expected.getName(), actual.getName());
    }

    mockRestServer.verify();
    mockRestServer.reset();
  }

  /**
   * Test for {@link DaemonStatusService#getAuStatus(String)}.
   */
  @Test
  public void testGetAuStatus() throws Exception {
    String auId = "test";

    AuStatus expectedResult = easyRandom.nextObject(AuStatus.class);

    // Prepare the URI path variables.
    Map<String, String> uriVariables = new HashMap<>(1);
    uriVariables.put("auId", auId);

    // Prepare the endpoint URI
    String auStatusesEndpoint = env.getProperty(CONFIG_SVC_URL_KEY) + "/austatuses/{auId}";
    URI auStatusesQuery = RestUtil.getRestUri(auStatusesEndpoint, uriVariables, null);

    mockRestServer
        .expect(ExpectedCount.once(), requestTo(auStatusesQuery))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("Authorization", BASIC_AUTH_HASH))
        .andRespond(withStatus(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(mapper.writeValueAsString(expectedResult)));

    // Make SOAP call
    AuStatus result = proxy.getAuStatus(auId);

    // Assert result
    assertEquals(expectedResult, result);

    mockRestServer.verify();
    mockRestServer.reset();
  }

  /**
   * Test for {@link DaemonStatusService#queryPlugins(String)}.
   */
  @Test
  public void testQueryPlugins() throws Exception {
    String pluginQuery = "plugin query";

    List<PluginWsResult> expectedResult =
        ListUtil.list(easyRandom.nextObject(PluginWsResult[].class));

    // Prepare the query parameters
    Map<String, String> queryParams = new HashMap<>(1);
    queryParams.put("pluginQuery", pluginQuery);

    // Prepare the endpoint URI
    String pluginsEndpoint = env.getProperty(CONFIG_SVC_URL_KEY) + "/plugins";
    URI pluginsQuery = RestUtil.getRestUri(pluginsEndpoint, null, queryParams);

    mockRestServer
        .expect(ExpectedCount.once(), requestTo(pluginsQuery))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("Authorization", BASIC_AUTH_HASH))
        .andRespond(withStatus(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(mapper.writeValueAsString(expectedResult)));

    // Make SOAP call
    List<PluginWsResult> result = proxy.queryPlugins(pluginQuery);

    // Assert result
    assertIterableEquals(expectedResult, result);

    mockRestServer.verify();
    mockRestServer.reset();
  }

  /**
   * Test for {@link DaemonStatusService#queryAus(String)}.
   */
  @Test
  public void testQueryAus() throws Exception {
    String auQuery = "au query";

    List<AuWsResult> expectedResult =
        ListUtil.list(easyRandom.nextObject(AuWsResult[].class));

    // Prepare the query parameters
    Map<String, String> queryParams = new HashMap<>(1);
    queryParams.put("auQuery", auQuery);

    // Prepare the endpoint URI
    String auQueriesEndpoint = env.getProperty(CONFIG_SVC_URL_KEY) + "/auqueries";
    URI auQueriesQuery = RestUtil.getRestUri(auQueriesEndpoint, null, queryParams);

    mockRestServer
        .expect(ExpectedCount.once(), requestTo(auQueriesQuery))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("Authorization", BASIC_AUTH_HASH))
        .andRespond(withStatus(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(mapper.writeValueAsString(expectedResult)));

    // Make SOAP call
    List<AuWsResult> result = proxy.queryAus(auQuery);

    // Assert result
    assertIterableEquals(expectedResult, result);

    mockRestServer.verify();
    mockRestServer.reset();
  }

  /**
   * Test for {@link DaemonStatusService#queryPeers(String)}.
   */
  @Test
  public void testQueryPeers() throws Exception {
    String peerQuery = "peer query";

    List<PeerWsResult> expectedResult =
        ListUtil.list(easyRandom.nextObject(PeerWsResult[].class));

    // Prepare the query parameters
    Map<String, String> queryParams = new HashMap<>(1);
    queryParams.put("peerQuery", peerQuery);

    // Prepare the endpoint URI
    String peersEndpoint = env.getProperty(POLLER_SVC_URL_KEY) + "/peers";
    URI peersQuery = RestUtil.getRestUri(peersEndpoint, null, queryParams);

    mockRestServer
        .expect(ExpectedCount.once(), requestTo(peersQuery))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("Authorization", BASIC_AUTH_HASH))
        .andRespond(withStatus(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(mapper.writeValueAsString(expectedResult)));

    // Make SOAP call
    List<PeerWsResult> result = proxy.queryPeers(peerQuery);

    // Assert result
    assertIterableEquals(expectedResult, result);

    mockRestServer.verify();
    mockRestServer.reset();
  }

  /**
   * Test for {@link DaemonStatusService#queryVotes(String)}.
   */
  @Test
  public void testQueryVotes() throws Exception {
    String voteQuery = "vote query";

    List<VoteWsResult> expectedResult =
        ListUtil.list(easyRandom.nextObject(VoteWsResult[].class));

    // Prepare the query parameters
    Map<String, String> queryParams = new HashMap<>(1);
    queryParams.put("voteQuery", voteQuery);

    // Prepare the endpoint URI
    String votesEndpoint = env.getProperty(POLLER_SVC_URL_KEY) + "/votes";
    URI votesQuery = RestUtil.getRestUri(votesEndpoint, null, queryParams);

    mockRestServer
        .expect(ExpectedCount.once(), requestTo(votesQuery))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("Authorization", BASIC_AUTH_HASH))
        .andRespond(withStatus(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(mapper.writeValueAsString(expectedResult)));

    // Make SOAP call
    List<VoteWsResult> result = proxy.queryVotes(voteQuery);

    // Assert expected result
    assertIterableEquals(expectedResult, result);

    mockRestServer.verify();
    mockRestServer.reset();
  }

  /**
   * Test for {@link DaemonStatusService#queryRepositorySpaces(String)}.
   */
  @Test
  public void testQueryRepositorySpaces() throws Exception {
    String repositorySpaceQuery = "repository space query";

    List<RepositorySpaceWsResult> expectedResult =
        ListUtil.list(easyRandom.nextObject(RepositorySpaceWsResult[].class));

    // Prepare the query parameters
    Map<String, String> queryParams = new HashMap<>(1);
    queryParams.put("repositorySpaceQuery", repositorySpaceQuery);

    // Prepare the endpoint URI
    String repoSpacesEndpoint = env.getProperty(POLLER_SVC_URL_KEY) + "/repositoryspaces";
    URI repoSpacesQuery = RestUtil.getRestUri(repoSpacesEndpoint, null, queryParams);

    mockRestServer
        .expect(ExpectedCount.once(), requestTo(repoSpacesQuery))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("Authorization", BASIC_AUTH_HASH))
        .andRespond(withStatus(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(mapper.writeValueAsString(expectedResult)));

    // Make SOAP call
    List<RepositorySpaceWsResult> result = proxy.queryRepositorySpaces(repositorySpaceQuery);

    // Assert result
    assertIterableEquals(expectedResult, result);

    mockRestServer.verify();
    mockRestServer.reset();
  }

  /**
   * Test for {@link DaemonStatusService#queryRepositories(String)}.
   */
  @Test
  public void testQueryRepositories() throws Exception {
    String repositoryQuery = "repository query";

    List<RepositoryWsResult> expectedResult =
        ListUtil.list(easyRandom.nextObject(RepositoryWsResult[].class));

    // Prepare the query parameters
    Map<String, String> queryParams = new HashMap<>(1);
    queryParams.put("repositoryQuery", repositoryQuery);

    // Prepare the endpoint URI
    String repositoriesEndpoint = env.getProperty(POLLER_SVC_URL_KEY) + "/repositories";
    URI repositoriesQuery = RestUtil.getRestUri(repositoriesEndpoint, null, queryParams);

    mockRestServer
        .expect(ExpectedCount.once(), requestTo(repositoriesQuery))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("Authorization", BASIC_AUTH_HASH))
        .andRespond(withStatus(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(mapper.writeValueAsString(expectedResult)));

    // Make SOAP call
    List<RepositoryWsResult> result = proxy.queryRepositories(repositoryQuery);

    // Assert result
    assertIterableEquals(expectedResult, result);

    mockRestServer.verify();
    mockRestServer.reset();
  }

  /**
   * Test for {@link DaemonStatusService#queryCrawls(String)}.
   */
  @Test
  public void testQueryCrawls() throws Exception {
    // TODO: Not implemented
  }

  /**
   * Test for {@link DaemonStatusService#queryPolls(String)}.
   */
  @Test
  public void testQueryPolls() throws Exception {
    String pollQuery = "repository query";

    List<PollWsResult> expectedResult =
        ListUtil.list(easyRandom.nextObject(PollWsResult[].class));

    // Prepare the query parameters
    Map<String, String> queryParams = new HashMap<>(1);
    queryParams.put("pollQuery", pollQuery);

    // Prepare the endpoint URI
    String pollsEndpoint = env.getProperty(POLLER_SVC_URL_KEY) + "/polls";
    URI pollsQuery = RestUtil.getRestUri(pollsEndpoint, null, queryParams);

    mockRestServer
        .expect(ExpectedCount.once(), requestTo(pollsQuery))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("Authorization", BASIC_AUTH_HASH))
        .andRespond(withStatus(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(mapper.writeValueAsString(expectedResult)));

    // Make SOAP call
    List<PollWsResult> result = proxy.queryPolls(pollQuery);

    // Assert result
    assertIterableEquals(expectedResult, result);

    mockRestServer.verify();
    mockRestServer.reset();
  }

  /**
   * Test for {@link DaemonStatusService#getPlatformConfiguration()}.
   */
  @Test
  public void testGetPlatformConfiguration() throws Exception {
    PlatformConfigurationWsResult expectedResult =
        easyRandom.nextObject(PlatformConfigurationWsResult.class);

    // Prepare the endpoint URI
    String platformConfigEndpoint = env.getProperty(CONFIG_SVC_URL_KEY) + "/config/platform";
    URI platformConfigQuery = RestUtil.getRestUri(platformConfigEndpoint, null, null);

    mockRestServer
        .expect(ExpectedCount.once(), requestTo(platformConfigQuery))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("Authorization", BASIC_AUTH_HASH))
        .andRespond(withStatus(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(mapper.writeValueAsString(expectedResult)));

    // Make SOAP call
    PlatformConfigurationWsResult result = proxy.getPlatformConfiguration();

    // Assert result
    assertEquals(expectedResult, result);

    mockRestServer.verify();
    mockRestServer.reset();
  }

  /**
   * Test for {@link DaemonStatusService#queryTdbPublishers(String)}.
   */
  @Test
  public void testQueryTdbPublishers() throws Exception {
    String tdbPublisherQuery = "tdb publisher query";

    List<TdbPublisherWsResult> expectedResult =
        ListUtil.list(easyRandom.nextObject(TdbPublisherWsResult[].class));

    // Prepare the query parameters
    Map<String, String> queryParams = new HashMap<>(1);
    queryParams.put("tdbPublisherQuery", tdbPublisherQuery);

    // Prepare the endpoint URI
    String tdbPublishersEndpoint = env.getProperty(CONFIG_SVC_URL_KEY) + "/tdbpublishers";
    URI tdbPublishersQuery = RestUtil.getRestUri(tdbPublishersEndpoint, null, queryParams);

    mockRestServer
        .expect(ExpectedCount.once(), requestTo(tdbPublishersQuery))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("Authorization", BASIC_AUTH_HASH))
        .andRespond(withStatus(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(mapper.writeValueAsString(expectedResult)));

    // Make SOAP call
    List<TdbPublisherWsResult> result = proxy.queryTdbPublishers(tdbPublisherQuery);

    // Assert result
    assertIterableEquals(expectedResult, result);

    mockRestServer.verify();
    mockRestServer.reset();
  }

  /**
   * Test for {@link DaemonStatusService#queryTdbTitles(String)}.
   */
  @Test
  public void testQueryTdbTitles() throws Exception {
    String tdbTitleQuery = "tdb title query";

    List<TdbTitleWsResult> expectedResult =
        ListUtil.list(easyRandom.nextObject(TdbTitleWsResult[].class));

    // Prepare the query parameters
    Map<String, String> queryParams = new HashMap<>(1);
    queryParams.put("tdbTitleQuery", tdbTitleQuery);

    // Prepare the endpoint URI
    String tdbTitlesEndpoint = env.getProperty(CONFIG_SVC_URL_KEY) + "/tdbtitles";
    URI tdbTitlesQuery = RestUtil.getRestUri(tdbTitlesEndpoint, null, queryParams);

    mockRestServer
        .expect(ExpectedCount.once(), requestTo(tdbTitlesQuery))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("Authorization", BASIC_AUTH_HASH))
        .andRespond(withStatus(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(mapper.writeValueAsString(expectedResult)));

    // Make SOAP call
    List<TdbTitleWsResult> result = proxy.queryTdbTitles(tdbTitleQuery);

    // Assert result
    assertIterableEquals(expectedResult, result);

    mockRestServer.verify();
    mockRestServer.reset();
  }

  /**
   * Test for {@link DaemonStatusService#queryTdbAus(String)}.
   */
  @Test
  public void testQueryTdbAus() throws Exception {
    String tdbAuQuery = "tdb au query";

    List<TdbAuWsResult> expectedResult =
        ListUtil.list(easyRandom.nextObject(TdbAuWsResult[].class));

    // Prepare the query parameters
    Map<String, String> queryParams = new HashMap<>(1);
    queryParams.put("tdbAuQuery", tdbAuQuery);

    // Prepare the endpoint URI
    String tdbAusEndpoint = env.getProperty(CONFIG_SVC_URL_KEY) + "/tdbaus";
    URI tdbAusQuery = RestUtil.getRestUri(tdbAusEndpoint, null, queryParams);

    mockRestServer
        .expect(ExpectedCount.once(), requestTo(tdbAusQuery))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("Authorization", BASIC_AUTH_HASH))
        .andRespond(withStatus(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(mapper.writeValueAsString(expectedResult)));

    // Make SOAP call
    List<TdbAuWsResult> result = proxy.queryTdbAus(tdbAuQuery);

    // Assert result
    assertIterableEquals(expectedResult, result);

    mockRestServer.verify();
    mockRestServer.reset();
  }

  /**
   * Test for {@link DaemonStatusService#getAuUrls(String, String)}.
   */
  @Test
  public void testGetAuUrls() throws Exception {
    String auId = "auId";
    String urlPrefix = "urlPrefix";

    List<Artifact> artifacts = ListUtil.list(easyRandom.nextObject(Artifact[].class));

    PageInfo pageInfo = new PageInfo();
    pageInfo.setResultsPerPage(artifacts.size());
    pageInfo.setTotalCount(artifacts.size());

    ArtifactPageInfo page = new ArtifactPageInfo();
    page.setPageInfo(pageInfo);
    page.setArtifacts(artifacts);

    List<String> expectedResult = artifacts.stream().map(Artifact::getUri).collect(Collectors.toList());

    // Prepare the URI path variables
    Map<String, String> uriVariables = new HashMap<>(1);
    uriVariables.put("collection", env.getProperty(REPO_COLLECTION_KEY));
    uriVariables.put("auId", auId);

    // Prepare the query parameters
    Map<String, String> queryParams = new HashMap<>(1);
    queryParams.put("urlPrefix", urlPrefix);

    // Prepare the endpoint URI
    String auArtifactsEndpoint = env.getProperty(REPO_SVC_URL_KEY) + "/collections/{collection}/aus/{auId}/artifacts";
    URI auArtifactsQuery = RestUtil.getRestUri(auArtifactsEndpoint, uriVariables, queryParams);

    mockRestServer
        .expect(ExpectedCount.once(), requestTo(auArtifactsQuery))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("Authorization", BASIC_AUTH_HASH))
        .andRespond(withStatus(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(mapper.writeValueAsString(page)));

    // Make SOAP call
    List<String> result = proxy.getAuUrls(auId, urlPrefix);

    // Assert result
    assertIterableEquals(expectedResult, result);

    mockRestServer.verify();
    mockRestServer.reset();
  }
}
