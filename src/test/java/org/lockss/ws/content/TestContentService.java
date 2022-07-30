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
import org.apache.http.ProtocolVersion;
import org.apache.http.message.BasicStatusLine;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lockss.app.*;
import org.lockss.laaws.rs.core.LockssRepository;
import org.lockss.laaws.rs.core.RestLockssRepository;
import org.lockss.laaws.rs.io.storage.warc.ArtifactState;
import org.lockss.laaws.rs.model.*;
import org.lockss.laaws.rs.util.ArtifactConstants;
import org.lockss.laaws.rs.util.ArtifactDataUtil;
import org.lockss.laaws.rs.util.NamedByteArrayResource;
import org.lockss.laaws.rs.util.NamedInputStreamResource;
import org.lockss.log.L4JLogger;
import org.lockss.spring.test.SpringLockssTestCase4;
import org.lockss.util.ListUtil;
import org.lockss.ws.entities.ContentResult;
import org.lockss.ws.entities.FileWsResult;
import org.lockss.ws.entities.LockssWebServicesFault;
import org.lockss.ws.test.BaseSoapTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"security.basic.enabled=false"})
public class TestContentService extends BaseSoapTest {
  private static final L4JLogger log = L4JLogger.getLogger();

  private static final String TARGET_NAMESPACE = "http://content.ws.lockss.org/";
  private static final String SERVICE_NAME = "ContentServiceImplService";
  private static final String ENDPOINT_NAME = "ContentService";

  private ContentService proxy;

  @Before
  public void init() throws Exception {
    proxy = setUpProxyAndCommonTestEnv(TARGET_NAMESPACE,
                                       ENDPOINT_NAME, SERVICE_NAME,
                                       ContentService.class);
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
        new URI(getServiceEndpoint(ServiceDescr.SVC_REPO) + "/collections/" + collection + "/aus/" + auid + "/artifacts");

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
      List<FileWsResult> results = proxy.getVersions(url, auid);

      // Assert artifacts match their respective FileWsResult
      assertEquals(artifact1, results.get(0));
      assertEquals(artifact2, results.get(1));

      mockRestServer.verify();
      mockRestServer.reset();
    }
  }

  /**
   * Asserts that an {@link Artifact} matches a {@link FileWsResult} derived from it.
   */
  private void assertEquals(Artifact expected, FileWsResult actual) {
    assertEquals(expected.getUri(), actual.getUrl());
    assertEquals(expected.getVersion(), actual.getVersion());
    assertEquals(expected.getContentLength(), (long)actual.getSize());
    assertEquals(expected.getCollectionDate(), (long)actual.getCollectionDate());
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
        new URI(getServiceEndpoint(ServiceDescr.SVC_REPO) + "/collections/" + collection + "/aus/" + auid + "/artifacts");

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
      assertTrue(result);

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
        new URI(getServiceEndpoint(ServiceDescr.SVC_REPO) + "/collections/" + collection + "/aus/" + auid + "/artifacts");

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
      assertTrue(proxy.isUrlVersionCached(url, auid, version));

      mockRestServer.verify();
      mockRestServer.reset();
    }
  }

  /**
   * Test for {@link ContentService#fetchFile(String, String)}.
   */
  @Test
  public void testFetchFile() throws Exception {
    {
      String url = "testUrl";
      String auid = null;
      assertThrows(LockssWebServicesFault.class, () -> proxy.fetchFile(url, auid),
          "Missing required Archival Unit identifier (auId)");
    }

    {
      String url = null;
      String auid = "testAuid";
      assertThrows(LockssWebServicesFault.class, () -> proxy.fetchFile(url, auid),
          "Missing required URL");
    }

    {
      String collection = "lockss";
      String auid = "testAuid";
      String url = "testUrl";
      String artifactId = "testArtifactId";

      // REST getArtifacts endpoint
      URI getArtifactsURL =
        new URI(getServiceEndpoint(ServiceDescr.SVC_REPO) + "/collections/" + collection + "/aus/" + auid + "/artifacts");

      URI getArtifactsQuery = UriComponentsBuilder.fromUri(getArtifactsURL)
          .queryParam("url", url)
          .queryParam("version", "latest")
          .build()
          .toUri();

      // Mock return object from REST getArtifacts call
      Artifact artifact =
          new Artifact(artifactId, collection, auid, url, 1, true,
              "file:///test.warc?offset=0&length=2014", 1024L, "digest");

//      artifact.setCollection(collection);
//      artifact.setAuid(auid);
//      artifact.setUri(url);
//      artifact.setVersion(1);
//      artifact.setContentLength(1024);
      artifact.setCollectionDate(1);
//      artifact.setCommitted(true);
//      artifact.setStorageUrl("file:///test.warc?offset=0&length=1024");

      PageInfo pageInfo = new PageInfo();
      pageInfo.setTotalCount(1);
      pageInfo.setResultsPerPage(1);
      pageInfo.setCurLink(getArtifactsQuery.toString());

      ArtifactPageInfo artifactsPage = new ArtifactPageInfo();
      artifactsPage.setPageInfo(pageInfo);
      artifactsPage.setArtifacts(ListUtil.list(artifact));

      // Mock REST call for Artifact
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(getArtifactsQuery))
          .andExpect(method(HttpMethod.GET))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andRespond(withStatus(HttpStatus.OK)
              .contentType(MediaType.APPLICATION_JSON)
              .body(mapper.writeValueAsString(artifactsPage)));

      // REST getArtifactData() endpoint
      URI getArtifactDataURL =
        new URI(getServiceEndpoint(ServiceDescr.SVC_REPO)
              + "/collections/" + collection
              + "/artifacts/" + artifactId);

      URI getArtifactDataQuery = UriComponentsBuilder.fromUri(getArtifactDataURL)
          .queryParam("includeContent", "ALWAYS")
          .build()
          .toUri();

      byte[] data = "hello world".getBytes(StandardCharsets.UTF_8);

      HttpHeaders props = new HttpHeaders();
      props.set("test", "xyzzy");

      ArtifactData artifactData = new ArtifactData(
          artifact.getIdentifier(),
          props,
          new ByteArrayInputStream(data),
          new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 200, "OK"),
          new URI(artifact.getStorageUrl()),
          ArtifactState.UNCOMMITTED);

      artifactData.setContentLength(data.length);
      artifactData.setContentDigest("testDigest");

      MultiValueMap<String, Object> parts = generateMultipartResponseFromArtifactData(
          artifactData, LockssRepository.IncludeContent.ALWAYS, 4096L);

      HttpOutputMessage outputMessage = new MockHttpOutputMessage();
      new AllEncompassingFormHttpMessageConverter().write(parts, MediaType.MULTIPART_FORM_DATA, outputMessage);

//      String responseBody = ((ByteArrayOutputStream) outputMessage.getBody()).toString();

//      // Set Content-Length of REST response body
//      HttpHeaders outputHeaders = outputMessage.getHeaders();
//      outputHeaders.setContentLength(responseBody.length());

      // Mock REST call for ArtifactData
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(getArtifactDataQuery))
          .andExpect(method(HttpMethod.GET))
          .andExpect(header("Accept", "multipart/form-data, application/json"))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andRespond(withStatus(HttpStatus.OK)
              .headers(outputMessage.getHeaders())
              .body(outputMessage.getBody().toString()));

      ContentResult contentResult = proxy.fetchFile(url, auid);

      // Assert content result properties
      Properties actualProps = contentResult.getProperties();
      assertNotNull(actualProps);
      assertIterableEquals(props.keySet(), actualProps.keySet());

      for (String key : props.keySet())
        assertEquals(props.getFirst(key), actualProps.getProperty(key));

      // Assert content result data
      DataHandler dh = contentResult.getDataHandler();
      assertNotNull(dh);
      assertInputStreamMatchesString("hello world", dh.getInputStream());
      assertEquals(MediaType.APPLICATION_OCTET_STREAM_VALUE, dh.getContentType());

      mockRestServer.verify();
      mockRestServer.reset();
    }
  }

  /**
   * Test for {@link ContentService#fetchVersionedFile(String, String, Integer)}.
   */
  @Test
  public void testFetchVersionedFile() throws Exception {
    {
      String collection = "lockss";
      String auid = "testAuid";
      String url = "testUrl";
      int version = 1;
      String artifactId = "testArtifactId";

      // REST getArtifacts endpoint
      URI getArtifactsURL =
        new URI(getServiceEndpoint(ServiceDescr.SVC_REPO) + "/collections/" + collection + "/aus/" + auid + "/artifacts");

      URI getArtifactsQuery = UriComponentsBuilder.fromUri(getArtifactsURL)
          .queryParam("url", url)
          .queryParam("version", version)
          .build()
          .toUri();

      // Mock return object from REST getArtifacts call
      Artifact artifact =
          new Artifact(artifactId, collection, auid, url, version, true,
              "file:///test.warc?offset=0&length=2014", 1024L, "digest");

//      artifact.setCollection(collection);
//      artifact.setAuid(auid);
//      artifact.setUri(url);
//      artifact.setVersion(version);
//      artifact.setContentLength(1024);
      artifact.setCollectionDate(1);
//      artifact.setCommitted(true);
//      artifact.setStorageUrl("file:///test.warc?offset=0&length=1024");

      PageInfo pageInfo = new PageInfo();
      pageInfo.setTotalCount(1);
      pageInfo.setResultsPerPage(1);
      pageInfo.setCurLink(getArtifactsQuery.toString());

      ArtifactPageInfo artifactsPage = new ArtifactPageInfo();
      artifactsPage.setPageInfo(pageInfo);
      artifactsPage.setArtifacts(ListUtil.list(artifact));

      // Mock REST call for Artifact
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(getArtifactsQuery))
          .andExpect(method(HttpMethod.GET))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andRespond(withStatus(HttpStatus.OK)
              .contentType(MediaType.APPLICATION_JSON)
              .body(mapper.writeValueAsString(artifactsPage)));

      // REST getArtifactData() endpoint
      URI getArtifactDataURL =
        new URI(getServiceEndpoint(ServiceDescr.SVC_REPO)
              + "/collections/" + collection
              + "/artifacts/" + artifactId);

      URI getArtifactDataQuery = UriComponentsBuilder.fromUri(getArtifactDataURL)
          .queryParam("includeContent", "ALWAYS")
          .build()
          .toUri();

      byte[] data = "hello world".getBytes(StandardCharsets.UTF_8);

      HttpHeaders props = new HttpHeaders();
      props.set("test", "xyzzy");

      ArtifactData artifactData = new ArtifactData(
          artifact.getIdentifier(),
          props,
          new ByteArrayInputStream(data),
          new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 200, "OK"),
          new URI(artifact.getStorageUrl()),
          ArtifactState.UNCOMMITTED);

      artifactData.setContentLength(data.length);
      artifactData.setContentDigest("testDigest");

      MultiValueMap<String, Object> parts = generateMultipartResponseFromArtifactData(
          artifactData, LockssRepository.IncludeContent.ALWAYS, 4096L);

      HttpOutputMessage outputMessage = new MockHttpOutputMessage();
      new AllEncompassingFormHttpMessageConverter().write(parts, MediaType.MULTIPART_FORM_DATA, outputMessage);

//      String responseBody = ((ByteArrayOutputStream) outputMessage.getBody()).toString();

//      // Set Content-Length of REST response body
//      HttpHeaders outputHeaders = outputMessage.getHeaders();
//      outputHeaders.setContentLength(responseBody.length());

      // Mock REST call for ArtifactData
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(getArtifactDataQuery))
          .andExpect(method(HttpMethod.GET))
          .andExpect(header("Accept", "multipart/form-data, application/json"))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andRespond(withStatus(HttpStatus.OK)
              .headers(outputMessage.getHeaders())
              .body(outputMessage.getBody().toString()));

      ContentResult contentResult = proxy.fetchVersionedFile(url, auid, version);

      // Assert content result properties
      Properties actualProps = contentResult.getProperties();
      assertNotNull(actualProps);
      assertIterableEquals(props.keySet(), actualProps.keySet());

      for (String key : props.keySet())
        assertEquals(props.getFirst(key), actualProps.getProperty(key));

      // Assert content result data
      DataHandler dh = contentResult.getDataHandler();
      assertNotNull(dh);
      assertInputStreamMatchesString("hello world", dh.getInputStream());
      assertEquals(MediaType.APPLICATION_OCTET_STREAM_VALUE, dh.getContentType());

      mockRestServer.verify();
      mockRestServer.reset();
    }
  }

  private static MultiValueMap<String, Object> generateMultipartResponseFromArtifactData(
      ArtifactData artifactData, LockssRepository.IncludeContent includeContent, long smallContentThreshold)
      throws IOException {

    // Get artifact ID
    String artifactid = artifactData.getIdentifier().getId();

    // Holds multipart response parts
    MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();

    //// Add artifact repository properties multipart
    {
      // Part's headers
      HttpHeaders partHeaders = new HttpHeaders();
      partHeaders.setContentType(MediaType.APPLICATION_JSON);

      // Add repository properties multipart to multiparts list
      parts.add(
          RestLockssRepository.MULTIPART_ARTIFACT_REPO_PROPS,
          new HttpEntity<>(getArtifactRepositoryProperties(artifactData), partHeaders)
      );
    }

    //// Add artifact headers multipart
    {
      // Part's headers
      HttpHeaders partHeaders = new HttpHeaders();
      partHeaders.setContentType(MediaType.APPLICATION_JSON);

      // Add artifact headers multipart
      parts.add(
          RestLockssRepository.MULTIPART_ARTIFACT_HEADER,
          new HttpEntity<>(artifactData.getMetadata(), partHeaders)
      );
    }

    //// Add artifact HTTP status multipart if present
    if (artifactData.getHttpStatus() != null) {
      // Part's headers
      HttpHeaders partHeaders = new HttpHeaders();
      partHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);

      // Create resource containing HTTP status byte array
      Resource resource = new NamedByteArrayResource(
          artifactid,
          ArtifactDataUtil.getHttpStatusByteArray(artifactData.getHttpStatus())
      );

      // Add artifact headers multipart
      parts.add(
          RestLockssRepository.MULTIPART_ARTIFACT_HTTP_STATUS,
          new HttpEntity<>(resource, partHeaders)
      );
    }

    //// Add artifact content part if requested or if small enough
    if ((includeContent == LockssRepository.IncludeContent.ALWAYS) ||
        (includeContent == LockssRepository.IncludeContent.IF_SMALL
            && artifactData.getContentLength() <= smallContentThreshold)) {

      // Create content part headers
      HttpHeaders partHeaders = new HttpHeaders();
      partHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
      partHeaders.setContentLength(artifactData.getContentLength());

      // Artifact content
      Resource resource = new NamedInputStreamResource(artifactid, artifactData.getInputStream());

      // Assemble content part and add to multiparts map
      parts.add(
          RestLockssRepository.MULTIPART_ARTIFACT_CONTENT,
          new HttpEntity<>(resource, partHeaders)
      );
    }

    return parts;
  }

  private static HttpHeaders getArtifactRepositoryProperties(ArtifactData ad) {
    HttpHeaders headers = new HttpHeaders();

    //// Artifact repository ID information headers
    ArtifactIdentifier id = ad.getIdentifier();
    headers.set(ArtifactConstants.ARTIFACT_ID_KEY, id.getId());
    headers.set(ArtifactConstants.ARTIFACT_COLLECTION_KEY, id.getCollection());
    headers.set(ArtifactConstants.ARTIFACT_AUID_KEY, id.getAuid());
    headers.set(ArtifactConstants.ARTIFACT_URI_KEY, id.getUri());
    headers.set(ArtifactConstants.ARTIFACT_VERSION_KEY, String.valueOf(id.getVersion()));

    ArtifactState state = ad.getArtifactState();

    //// Artifact repository state information headers if present
    if (state != null) {
      headers.set(
          ArtifactConstants.ARTIFACT_STATE_COMMITTED,
          String.valueOf(state.isCommitted())
      );

      headers.set(
          ArtifactConstants.ARTIFACT_STATE_DELETED,
          String.valueOf(state.isDeleted())
      );
    }

    //// Unclassified artifact repository headers
    headers.set(ArtifactConstants.ARTIFACT_LENGTH_KEY, String.valueOf(ad.getContentLength()));
    headers.set(ArtifactConstants.ARTIFACT_DIGEST_KEY, ad.getContentDigest());

//    headers.set(ArtifactConstants.ARTIFACT_ORIGIN_KEY, ???);
//    headers.set(ArtifactConstants.ARTIFACT_COLLECTION_DATE, ???);

    return headers;
  }
}
