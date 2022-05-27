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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lockss.log.L4JLogger;
import org.lockss.spring.test.SpringLockssTestCase4;
import org.lockss.util.ListUtil;
import org.lockss.util.rest.RestResponseErrorBody;
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

  private static final ObjectMapper mapper = new ObjectMapper();

  private static final String TARGET_NAMESPACE = "http://status.ws.lockss.org/";
  private static final String SERVICE_NAME = "DaemonStatusServiceImplService";

  private static final String USERNAME = "lockss-u";
  private static final String PASSWORD = "lockss-p";
  private static final String BASIC_AUTH_HASH = "Basic bG9ja3NzLXU6bG9ja3NzLXA=";

  // FIXME: Blank mock REST error response
  private static final RestResponseErrorBody.RestResponseError blankError =
      new RestResponseErrorBody.RestResponseError();

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
  }

  /**
   * Test for {@link DaemonStatusService#isDaemonReady()}.
   */
  @Test
  public void testIsDaemonReady() throws Exception {
    mockIsServiceReady(env.getProperty(REPO_SVC_URL_KEY), true);
    mockIsServiceReady(env.getProperty(CONFIG_SVC_URL_KEY), true);
    mockIsServiceReady(env.getProperty(POLLER_SVC_URL_KEY), true);
    mockIsServiceReady(env.getProperty(MDX_SVC_URL_KEY), true);
    mockIsServiceReady(env.getProperty(MDQ_SVC_URL_KEY), true);

    boolean result = proxy.isDaemonReady();

    assertTrue(result);
  }

  private void mockIsServiceReady(String url, boolean isReady) throws Exception {
    // Prepare the endpoint URI
    String statusEndpoint = url + "/status";
    URI statusQuery = RestUtil.getRestUri(statusEndpoint, null, null);

    ApiStatus apiStatus = new ApiStatus();
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
    List<String> suffixes = ListUtil.list("A", "B", "C");
    List<AuWsResult> expectedResult = new ArrayList<>(); // TODO

    for (String suffix : suffixes) {
      AuWsResult result = new AuWsResult();
      result.setAuId("auid" + suffix);
      result.setName("name" + suffix);

      expectedResult.add(result);
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
            .body(mapper.writeValueAsString(expectedResult)));

    Collection<IdNamePair> result = proxy.getAuIds();

    assertEquals(expectedResult.size(), result.size());
    // TODO: Assert
  }

  /**
   * Test for {@link DaemonStatusService#getAuStatus(String)}.
   */
  @Test
  public void testGetAuStatus() throws Exception {
    String auId = "test";

    AuStatus expectedResult = new AuStatus(); // TODO

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

    AuStatus result = proxy.getAuStatus(auId);

    // TODO: Assert
  }

  /**
   * Test for {@link DaemonStatusService#queryPlugins(String)}.
   */
  @Test
  public void testQueryPlugins() throws Exception {
    String pluginQuery = "plugin query";

    List<PluginWsResult> expectedResult = new ArrayList<>(); // TODO

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

    List<PluginWsResult> result = proxy.queryPlugins(pluginQuery);

    // TODO: Assert
  }

  /**
   * Test for {@link DaemonStatusService#queryAus(String)}.
   */
  @Test
  public void testQueryAus() throws Exception {
    String auQuery = "au query";

    List<AuWsResult> expectedResult = new ArrayList<>(); // TODO

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

    List<AuWsResult> result = proxy.queryAus(auQuery);

    // TODO: Assert
  }

  /**
   * Test for {@link DaemonStatusService#queryPeers(String)}.
   */
  @Test
  public void testQueryPeers() throws Exception {
    String peerQuery = "peer query";

    List<PeerWsResult> expectedResult = new ArrayList<>(); // TODO

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

    List<PeerWsResult> result = proxy.queryPeers(peerQuery);

    // TODO: Assert
  }

  /**
   * Test for {@link DaemonStatusService#queryVotes(String)}.
   */
  @Test
  public void testQueryVotes() throws Exception {
    String voteQuery = "vote query";

    List<VoteWsResult> expectedResult = new ArrayList<>(); // TODO

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

    List<VoteWsResult> result = proxy.queryVotes(voteQuery);

    // TODO: Assert
  }

  /**
   * Test for {@link DaemonStatusService#queryRepositorySpaces(String)}.
   */
  @Test
  public void testQueryRepositorySpaces() throws Exception {
    String repositorySpaceQuery = "repository space query";

    List<VoteWsResult> expectedResult = new ArrayList<>(); // TODO

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

    List<RepositorySpaceWsResult> result = proxy.queryRepositorySpaces(repositorySpaceQuery);

    // TODO: Assert
  }

  /**
   * Test for {@link DaemonStatusService#queryRepositories(String)}.
   */
  @Test
  public void testQueryRepositories() throws Exception {
    String repositoryQuery = "repository query";

    List<RepositoryWsResult> expectedResult = new ArrayList<>(); // TODO

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

    List<RepositoryWsResult> result = proxy.queryRepositories(repositoryQuery);

    // TODO: Assert
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

    List<PollWsResult> expectedResult = new ArrayList<>(); // TODO

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

    List<PollWsResult> result = proxy.queryPolls(pollQuery);

    // TODO: Assert
  }

  /**
   * Test for {@link DaemonStatusService#getPlatformConfiguration()}.
   */
  @Test
  public void testGetPlatformConfiguration() throws Exception {
    PlatformConfigurationWsResult expectedResult = new PlatformConfigurationWsResult(); // TODO

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

    PlatformConfigurationWsResult result = proxy.getPlatformConfiguration();

    // TODO: Assert
  }

  /**
   * Test for {@link DaemonStatusService#queryTdbPublishers(String)}.
   */
  @Test
  public void testQueryTdbPublishers() throws Exception {
    String tdbPublisherQuery = "tdb publisher query";

    List<TdbPublisherWsResult> expectedResult = new ArrayList<>(); // TODO

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

    List<TdbPublisherWsResult> result = proxy.queryTdbPublishers(tdbPublisherQuery);

    // TODO: Assert
  }

  /**
   * Test for {@link DaemonStatusService#queryTdbTitles(String)}.
   */
  @Test
  public void testQueryTdbTitles() throws Exception {
    String tdbTitleQuery = "tdb title query";

    List<TdbTitleWsResult> expectedResult = new ArrayList<>(); // TODO

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

    List<TdbTitleWsResult> result = proxy.queryTdbTitles(tdbTitleQuery);

    // TODO: Assert
  }

  /**
   * Test for {@link DaemonStatusService#queryTdbAus(String)}.
   */
  @Test
  public void testQueryTdbAus() throws Exception {
    String tdbAuQuery = "tdb au query";

    List<TdbAuWsResult> expectedResult = new ArrayList<>(); // TODO

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

    List<TdbAuWsResult> result = proxy.queryTdbAus(tdbAuQuery);

    // TODO: Assert
  }

  /**
   * Test for {@link DaemonStatusService#getAuUrls(String, String)}.
   */
  @Test
  public void testGetAuUrls() throws Exception {
    String auId = "auId";
    String urlPrefix = "urlPrefix";

    List<String> expectedResult = new ArrayList<>(); // TODO

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
            .body(mapper.writeValueAsString(expectedResult)));

    List<String> result = proxy.getAuUrls(auId, urlPrefix);

    // TODO: Assert
  }
}
