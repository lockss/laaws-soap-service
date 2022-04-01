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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lockss.laaws.rs.model.Artifact;
import org.lockss.laaws.rs.model.ArtifactPageInfo;
import org.lockss.laaws.rs.model.PageInfo;
import org.lockss.log.L4JLogger;
import org.lockss.spring.test.SpringLockssTestCase4;
import org.lockss.util.ListUtil;
import org.lockss.util.rest.RestResponseErrorBody;
import org.lockss.ws.entities.FileWsResult;
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
import org.springframework.web.util.UriComponentsBuilder;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static org.lockss.ws.BaseServiceImpl.REPO_SVC_URL_KEY;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"security.basic.enabled=false"})
public class TestContentService extends SpringLockssTestCase4 {
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

  private ContentService proxy;
  private MockRestServiceServer mockRestServer;

  private static final ObjectMapper mapper = new ObjectMapper();

  private static final String TARGET_NAMESPACE = "http://content.ws.lockss.org/";
  private static final String SERVICE_NAME = "ContentServiceImplService";

  private static final String USERNAME = "lockss-u";
  private static final String PASSWORD = "lockss-p";
  private static final String BASIC_AUTH_HASH = "Basic bG9ja3NzLXU6bG9ja3NzLXA=";

  // FIXME: Blank mock REST error response
  private static final RestResponseErrorBody.RestResponseError blankError =
      new RestResponseErrorBody.RestResponseError();

  @Before
  public void init() throws MalformedURLException {
    // Setup proxy to SOAP service
    String wsdlEndpoint = "http://localhost:" + port + "/ws/ContentService?wsdl";
    Service srv = Service.create(new URL(wsdlEndpoint), new QName(TARGET_NAMESPACE, SERVICE_NAME));
    proxy = srv.getPort(ContentService.class);

    // Add authentication headers for SOAP request
    BindingProvider bp = (BindingProvider) proxy;
    Map<String, Object> requestContext = bp.getRequestContext();
    requestContext.put(BindingProvider.USERNAME_PROPERTY, USERNAME);
    requestContext.put(BindingProvider.PASSWORD_PROPERTY, PASSWORD);

    // Create MockRestServiceServer from RestTemplate
    mockRestServer = MockRestServiceServer.createServer(restTemplate);
  }

  /**
   * Test for {@link ContentService#getVersions(String, String)}.
   */
  @Test
  public void testGetVersions() throws Exception {
    //// Test success
    {
      // Parameters to SOAP and REST calls
      String collection = "lockss";
      String auid = "testAuid";
      String url = "testUrl";

      URI auArtifactsEndpoint =
          new URI(env.getProperty(REPO_SVC_URL_KEY) + "/collections/" + collection + "/aus/" + auid + "/artifacts");

      URI allUrlVersionsEndpoint_p1 = UriComponentsBuilder.fromUri(auArtifactsEndpoint)
          .queryParam("url", url)
          .queryParam("version", "all")
          .build()
          .toUri();

      URI allUrlVersionsEndpoint_p2 = UriComponentsBuilder.fromUri(auArtifactsEndpoint)
          .queryParam("url", url)
          .queryParam("version", "all")
          .queryParam("continuationToken", "ABC")
          .build()
          .toUri();

      // Page 1 of artifacts

      Artifact artifact1 = new Artifact();
      artifact1.setCollection(collection);
      artifact1.setAuid(auid);
      artifact1.setUri(url);
      artifact1.setVersion(1);
      artifact1.setContentLength(1234);
      artifact1.setCollectionDate(1);
      artifact1.setCommitted(true);
      artifact1.setStorageUrl("file://test.warc?offset=0&length=1234");

      PageInfo pageInfo_p1 = new PageInfo();
      pageInfo_p1.setTotalCount(2);
      pageInfo_p1.setResultsPerPage(1);
      pageInfo_p1.setCurLink(allUrlVersionsEndpoint_p1.toString());
      pageInfo_p1.setContinuationToken("ABC");
      pageInfo_p1.setNextLink(allUrlVersionsEndpoint_p2.toString());

      ArtifactPageInfo artifactPageInfo_p1 = new ArtifactPageInfo();
      artifactPageInfo_p1.setPageInfo(pageInfo_p1);
      artifactPageInfo_p1.setArtifacts(ListUtil.list(artifact1));

      // Page 2 of artifacts

      Artifact artifact2 = new Artifact();
      artifact2.setCollection(collection);
      artifact2.setAuid(auid);
      artifact2.setUri(url);
      artifact2.setVersion(2);
      artifact2.setContentLength(1234);
      artifact2.setCollectionDate(2);
      artifact2.setCommitted(true);
      artifact2.setStorageUrl("file://test.warc?offset=1234&length=1234");

      PageInfo pageInfo_p2 = new PageInfo();
      pageInfo_p2.setTotalCount(2);
      pageInfo_p2.setResultsPerPage(1);
      pageInfo_p2.setCurLink(allUrlVersionsEndpoint_p2.toString());

      ArtifactPageInfo artifactPageInfo_p2 = new ArtifactPageInfo();
      artifactPageInfo_p2.setPageInfo(pageInfo_p2);
      artifactPageInfo_p2.setArtifacts(ListUtil.list(artifact2));

      // Mock REST service call and response (page 1)
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(allUrlVersionsEndpoint_p1))
          .andExpect(method(HttpMethod.GET))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andRespond(withStatus(HttpStatus.OK)
              .contentType(MediaType.APPLICATION_JSON)
              .body(mapper.writeValueAsString(artifactPageInfo_p1)));

      // Mock REST service call and response (page 2)
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(allUrlVersionsEndpoint_p2))
          .andExpect(method(HttpMethod.GET))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andRespond(withStatus(HttpStatus.OK)
              .contentType(MediaType.APPLICATION_JSON)
              .body(mapper.writeValueAsString(artifactPageInfo_p2)));

      // Make the call through SOAP
      List<FileWsResult> result = proxy.getVersions(url, auid);

      log.info("result = {}", result);

      mockRestServer.verify();
      mockRestServer.reset();
    }
  }

  /**
   * Test for {@link ContentService#isUrlCached(String, String)}.
   */
  @Test
  public void testIsUrlCached() throws Exception {
    //// Test success
    {
      // Parameters to SOAP and REST calls
      String collection = "lockss";
      String auid = "testAuid";
      String url = "testUrl";

      URI auArtifactsEndpoint =
          new URI(env.getProperty(REPO_SVC_URL_KEY) + "/collections/" + collection + "/aus/" + auid + "/artifacts");

      URI allUrlVersionsEndpoint = UriComponentsBuilder.fromUri(auArtifactsEndpoint)
          .queryParam("url", url)
          .queryParam("version", "latest")
          .build()
          .toUri();

      Artifact artifact = new Artifact();
      artifact.setCollection(collection);
      artifact.setAuid(auid);
      artifact.setUri(url);
      artifact.setVersion(1);
      artifact.setContentLength(1234);
      artifact.setCollectionDate(1);
      artifact.setCommitted(true);
      artifact.setStorageUrl("file://test.warc?offset=0&length=1234");

      PageInfo pageInfo = new PageInfo();
      pageInfo.setTotalCount(1);
      pageInfo.setResultsPerPage(1);
      pageInfo.setCurLink(allUrlVersionsEndpoint.toString());

      ArtifactPageInfo artifactPageInfo = new ArtifactPageInfo();
      artifactPageInfo.setPageInfo(pageInfo);
      artifactPageInfo.setArtifacts(ListUtil.list(artifact));

      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(allUrlVersionsEndpoint))
          .andExpect(method(HttpMethod.GET))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andRespond(withStatus(HttpStatus.OK)
              .contentType(MediaType.APPLICATION_JSON)
              .body(mapper.writeValueAsString(artifactPageInfo)));

      // Make the call through SOAP
      boolean result = proxy.isUrlCached(url, auid);

      log.info("result = {}", result);

      mockRestServer.verify();
      mockRestServer.reset();
    }
  }

  /**
   * Test for {@link ContentService#isUrlVersionCached(String, String, Integer)}.
   */
  @Test
  public void testIsUrlVersionCached() throws Exception {
    //// Test success
    {
      // Parameters to SOAP and REST calls
      String collection = "lockss";
      String auid = "testAuid";
      String url = "testUrl";
      int version = 1234;

      URI auArtifactsEndpoint =
          new URI(env.getProperty(REPO_SVC_URL_KEY) + "/collections/" + collection + "/aus/" + auid + "/artifacts");

      URI allUrlVersionsEndpoint = UriComponentsBuilder.fromUri(auArtifactsEndpoint)
          .queryParam("url", url)
          .queryParam("version", version)
          .build()
          .toUri();

      Artifact artifact = new Artifact();
      artifact.setCollection(collection);
      artifact.setAuid(auid);
      artifact.setUri(url);
      artifact.setVersion(version);
      artifact.setContentLength(1234);
      artifact.setCollectionDate(1);
      artifact.setCommitted(true);
      artifact.setStorageUrl("file://test.warc?offset=0&length=1234");

      PageInfo pageInfo = new PageInfo();
      pageInfo.setTotalCount(1);
      pageInfo.setResultsPerPage(1);
      pageInfo.setCurLink(allUrlVersionsEndpoint.toString());

      ArtifactPageInfo artifactPageInfo = new ArtifactPageInfo();
      artifactPageInfo.setPageInfo(pageInfo);
      artifactPageInfo.setArtifacts(ListUtil.list(artifact));

      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(allUrlVersionsEndpoint))
          .andExpect(method(HttpMethod.GET))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andRespond(withStatus(HttpStatus.OK)
              .contentType(MediaType.APPLICATION_JSON)
              .body(mapper.writeValueAsString(artifactPageInfo)));

      // Make the call through SOAP
      boolean result = proxy.isUrlVersionCached(url, auid, version);

      log.info("result = {}", result);

      mockRestServer.verify();
      mockRestServer.reset();
    }
  }
}
