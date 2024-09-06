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

import jakarta.activation.DataHandler;
import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.impl.io.HttpTransportMetricsImpl;
import org.apache.http.impl.io.SessionOutputBufferImpl;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicLineFormatter;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.util.CharArrayBuffer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lockss.app.ServiceDescr;
import org.lockss.log.L4JLogger;
import org.lockss.util.ListUtil;
import org.lockss.util.rest.repo.LockssRepository;
import org.lockss.util.rest.repo.RestLockssRepository;
import org.lockss.util.rest.repo.model.*;
import org.lockss.util.rest.repo.util.ArtifactConstants;
import org.lockss.util.rest.repo.util.ArtifactDataUtil;
import org.lockss.util.rest.repo.util.ArtifactSpec;
import org.lockss.util.rest.repo.util.NamedByteArrayResource;
import org.lockss.ws.SoapApplication;
import org.lockss.ws.entities.ContentResult;
import org.lockss.ws.entities.FileWsResult;
import org.lockss.ws.entities.LockssWebServicesFault;
import org.lockss.ws.test.BaseSoapTest;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = {SoapApplication.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"security.basic.enabled=false"})
public class TestContentService extends BaseSoapTest {
  private static final L4JLogger log = L4JLogger.getLogger();

  private static final String TARGET_NAMESPACE = "http://content.ws.lockss.org/";
  private static final String SERVICE_NAME = "ContentServiceImplService";
  private static final String ENDPOINT_NAME = "ContentService";
  public static final String APPLICATION_HTTP_RESPONSE_VALUE =
      "application/http;msgtype=response";
  public static final MediaType APPLICATION_HTTP_RESPONSE =
      MediaType.parseMediaType(APPLICATION_HTTP_RESPONSE_VALUE);

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
      String namespace = "lockss";
      String auid = "testAuid";
      String url = "testUrl";

      URI auArtifactsEndpoint =
        new URI(getServiceEndpoint(ServiceDescr.SVC_REPO) + "/aus/" + auid + "/artifacts");

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
      artifact1.setNamespace(namespace);
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
      artifact2.setNamespace(namespace);
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
      String namespace = "lockss";
      String auid = "testAuid";
      String url = "testUrl";

      URI auArtifactsEndpoint =
        new URI(getServiceEndpoint(ServiceDescr.SVC_REPO) + "/aus/" + auid + "/artifacts");

      URI allUrlVersionsEndpoint = UriComponentsBuilder.fromUri(auArtifactsEndpoint)
          .queryParam("url", url)
          .queryParam("version", "latest")
          .build()
          .toUri();

      Artifact artifact = new Artifact();
      artifact.setNamespace(namespace);
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
      String namespace = "lockss";
      String auid = "testAuid";
      String url = "testUrl";
      int version = 1234;

      URI auArtifactsEndpoint =
        new URI(getServiceEndpoint(ServiceDescr.SVC_REPO) + "/aus/" + auid + "/artifacts");

      URI allUrlVersionsEndpoint = UriComponentsBuilder.fromUri(auArtifactsEndpoint)
          .queryParam("url", url)
          .queryParam("version", version)
          .build()
          .toUri();

      Artifact artifact = new Artifact();
      artifact.setNamespace(namespace);
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
      String namespace = "lockss";
      String auid = "testAuid";
      String url = "testUrl";
      String artifactId = "testArtifactId";

      HttpHeaders props = new HttpHeaders();
      props.set("test", "xyzzy");

      ArtifactSpec spec = new ArtifactSpec()
          .setNamespace(namespace)
          .setArtifactUuid(artifactId)
          .setAuid(auid)
          .setUrl(url)
          .setHeaders(props.toSingleValueMap())
          .setStorageUrl(new URI("artifact-storageUrl"))
          .generateContent();

      // Get the ContentServiceImpl bean from the application context and set its
      // RestLockssRepository to a mock that we can control:
      ContentServiceImpl svcImpl = appCtx.getBean(ContentServiceImpl.class);
      RestLockssRepository repoClient = Mockito.mock(RestLockssRepository.class);
      svcImpl.setRestLockssRepository(repoClient);

      // Mock REST call for getArtifact
      when(repoClient.getArtifact(
          ArgumentMatchers.any(), eq(auid), eq(url)))
          .thenReturn(spec.getArtifact());

//      ResourceHttpMessageConverter converter = new ResourceHttpMessageConverter();
//      MockHttpOutputMessage outputMessage = new MockHttpOutputMessage();
//      InputStreamResource resource = new InputStreamResource(
//          ArtifactDataUtil.getHttpResponseStreamFromArtifactData(artifactData));
//      converter.write(resource, APPLICATION_HTTP_RESPONSE, outputMessage);

      // Mock REST call for getArtifactData
      when(repoClient.getArtifactData(
          argThat(artifact -> artifact.equals(spec.getArtifact())),
          eq(LockssRepository.IncludeContent.ALWAYS)))
          .thenReturn(spec.getArtifactData());

      ContentResult contentResult = proxy.fetchFile(url, auid);

      // Assert content result properties
      Properties actualProps = contentResult.getProperties();
      actualProps.remove("Content-Type");
      assertNotNull(actualProps);
      assertIterableEquals(props.keySet(), actualProps.keySet());

      for (String key : props.keySet())
        assertEquals(props.getFirst(key), actualProps.getProperty(key));

      // Assert content result data
      DataHandler dh = contentResult.getDataHandler();
      assertNotNull(dh);
      assertInputStreamMatchesString(spec.getContent(), dh.getInputStream());
      assertEquals(MediaType.APPLICATION_OCTET_STREAM_VALUE, dh.getContentType());

      svcImpl.setRestLockssRepository(null);
      mockRestServer.verify();
      mockRestServer.reset();
    }
  }

  private URI endpointOfGetArtifactData(String artifactUuid) throws URISyntaxException {
    return new URI(getServiceEndpoint(ServiceDescr.SVC_REPO)
        + "/artifacts/" + artifactUuid + "/response");
  }

  /**
   * Test for {@link ContentService#fetchVersionedFile(String, String, Integer)}.
   */
  @Test
  public void testFetchVersionedFile() throws Exception {
    {
      String namespace = "lockss";
      String auid = "testAuid";
      String url = "testUrl";
      int version = 1;
      String artifactId = "testArtifactId";

      HttpHeaders props = new HttpHeaders();
      props.set("test", "xyzzy");

      ArtifactSpec spec = new ArtifactSpec()
          .setNamespace(namespace)
          .setArtifactUuid(artifactId)
          .setAuid(auid)
          .setUrl(url)
          .setHeaders(props.toSingleValueMap())
          .setStorageUrl(new URI("artifact-storageUrl"))
          .generateContent();

      // Get the ContentServiceImpl bean from the application context and set its
      // RestLockssRepository to a mock that we can control:
      ContentServiceImpl svcImpl = appCtx.getBean(ContentServiceImpl.class);
      RestLockssRepository repoClient = Mockito.mock(RestLockssRepository.class);
      svcImpl.setRestLockssRepository(repoClient);

      // Mock REST call for getArtifactVersion
      when(repoClient.getArtifactVersion(
          ArgumentMatchers.any(), eq(auid), eq(url), eq(version), eq(false)))
          .thenReturn(spec.getArtifact());

      // Mock REST call for getArtifactData
      when(repoClient.getArtifactData(
          argThat(artifact -> artifact.equals(spec.getArtifact())),
          eq(LockssRepository.IncludeContent.ALWAYS)))
          .thenReturn(spec.getArtifactData());

      ContentResult contentResult = proxy.fetchVersionedFile(url, auid, version);

      // Assert content result properties
      Properties actualProps = contentResult.getProperties();
      actualProps.remove("Content-Type");
      assertNotNull(actualProps);
      assertIterableEquals(props.keySet(), actualProps.keySet());

      for (String key : props.keySet())
        assertEquals(props.getFirst(key), actualProps.getProperty(key));

      // Assert content result data
      DataHandler dh = contentResult.getDataHandler();
      assertNotNull(dh);
      assertInputStreamMatchesString(spec.getContent(), dh.getInputStream());
      assertEquals(MediaType.APPLICATION_OCTET_STREAM_VALUE, dh.getContentType());

      svcImpl.setRestLockssRepository(null);
      mockRestServer.verify();
      mockRestServer.reset();
    }
  }

  public static byte[] getHttpStatusByteArray(StatusLine httpStatus) throws IOException {
    UnsynchronizedByteArrayOutputStream output = new UnsynchronizedByteArrayOutputStream();
    CharArrayBuffer lineBuf = new CharArrayBuffer(128);

    // Create a new SessionOutputBuffer and bind the UnsynchronizedByteArrayOutputStream
    SessionOutputBufferImpl outputBuffer = new SessionOutputBufferImpl(new HttpTransportMetricsImpl(),4096);
    outputBuffer.bind(output);

    // Write HTTP status line
    BasicLineFormatter.INSTANCE.formatStatusLine(lineBuf, httpStatus);
    outputBuffer.writeLine(lineBuf);
    outputBuffer.flush();

    // Flush and close UnsynchronizedByteArrayOutputStream
    output.flush();
    output.close();

    // Return HTTP status byte array
    return output.toByteArray();
  }

  public static MultiValueMap<String, Object> generateMultipartMapFromArtifactData(
      ArtifactData artifactData, LockssRepository.IncludeContent includeContent, long smallContentThreshold)
      throws IOException {

    String artifactUuid = artifactData.getIdentifier().getUuid();

    // Holds multipart response parts
    MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();

    //// Add artifact repository properties multipart
    {
      // Part's headers
      HttpHeaders partHeaders = new HttpHeaders();
      partHeaders.setContentType(MediaType.APPLICATION_JSON);

      // Add repository properties multipart to multiparts list
      parts.add(RestLockssRepository.MULTIPART_ARTIFACT_PROPS,
          new HttpEntity<>(getArtifactProperties(artifactData), partHeaders));
    }

    //// Add HTTP response header multiparts if present
    if (artifactData.isHttpResponse()) {
      //// HTTP status part
      {
        // Part's headers
        HttpHeaders partHeaders = new HttpHeaders();
        partHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        HttpResponse httpResponse = new BasicHttpResponse(artifactData.getHttpStatus());

        httpResponse.setHeaders(
            ArtifactDataUtil.transformHttpHeadersToHeaderArray(artifactData.getHttpHeaders()));

        byte[] header = ArtifactDataUtil.getHttpResponseHeader(httpResponse);

        // Create resource containing HTTP status byte array
        Resource resource = new NamedByteArrayResource(artifactUuid, header);

        // Add artifact headers multipart
        parts.add(RestLockssRepository.MULTIPART_ARTIFACT_HTTP_RESPONSE_HEADER,
            new HttpEntity<>(resource, partHeaders));
      }
    }

    //// Add artifact content part if requested or if small enough
    if ((includeContent == LockssRepository.IncludeContent.ALWAYS) ||
        (includeContent == LockssRepository.IncludeContent.IF_SMALL
            && artifactData.getContentLength() <= smallContentThreshold)) {

      // Create content part headers
      HttpHeaders partHeaders = new HttpHeaders();

      if (artifactData.hasContentLength()) {
        partHeaders.setContentLength(artifactData.getContentLength());
      }

      HttpHeaders artifactHeaders = artifactData.getHttpHeaders();

      // Attempt to parse and set the Content-Type of the part using MediaType. If the Content-Type is not
      // specified (null) then omit the header. If an error occurs due to an malformed Content-Type, set
      // the X-Lockss-Content-Type to the malformed value and omit the Content-Type header.
      try {
        MediaType type = artifactHeaders.getContentType();
        if (type != null) {
          partHeaders.setContentType(type);
        }
      } catch (InvalidMediaTypeException e) {
        partHeaders.set(ArtifactConstants.X_LOCKSS_CONTENT_TYPE,
            artifactHeaders.getFirst(HttpHeaders.CONTENT_TYPE));
      }

      // FIXME: Filename must be set or else Spring will treat the part as a parameter instead of a file
      partHeaders.setContentDispositionFormData(
          RestLockssRepository.MULTIPART_ARTIFACT_PAYLOAD, RestLockssRepository.MULTIPART_ARTIFACT_PAYLOAD);

      // Artifact content
//      InputStreamResource resource = new NamedInputStreamResource(artifactUuid, artifactData.getInputStream());
      InputStreamResource resource = new InputStreamResource(artifactData.getInputStream());

      // Assemble content part and add to multiparts map
      parts.add(RestLockssRepository.MULTIPART_ARTIFACT_PAYLOAD,
          new HttpEntity<>(resource, partHeaders));
    }

    return parts;
  }

  private static Map<String, String> getArtifactProperties(ArtifactData ad) {
    Map<String, String> props = new HashMap<>();
    ArtifactIdentifier id = ad.getIdentifier();

    putIfNotNull(props, Artifact.ARTIFACT_NAMESPACE_KEY, id.getNamespace());
    putIfNotNull(props, Artifact.ARTIFACT_UUID_KEY, id.getUuid());
    props.put(Artifact.ARTIFACT_AUID_KEY, id.getAuid());
    props.put(Artifact.ARTIFACT_URI_KEY, id.getUri());

    if (id.getVersion() > 0) {
      props.put(Artifact.ARTIFACT_VERSION_KEY, String.valueOf(id.getVersion()));
    }

    if (ad.hasContentLength()) {
      props.put(Artifact.ARTIFACT_LENGTH_KEY, String.valueOf(ad.getContentLength()));
    }

    putIfNotNull(props, Artifact.ARTIFACT_DIGEST_KEY, ad.getContentDigest());
    putIfNonZero(props, Artifact.ARTIFACT_COLLECTION_DATE_KEY, ad.getCollectionDate());

    return props;
  }

  private static void putIfNonZero(Map props, String k, long v) {
    if (v == 0) return;
    props.put(k, String.valueOf(v));
  }

  private static void putIfNotNull(Map props, String k, String v) {
    if (v == null) return;
    props.put(k, v);
  }
}
