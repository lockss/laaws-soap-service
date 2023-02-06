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
package org.lockss.ws.importer;

import com.sun.istack.ByteArrayDataSource;
import org.apache.commons.fileupload.FileItem;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lockss.app.ServiceDescr;
import org.lockss.log.L4JLogger;
import org.lockss.util.ListUtil;
import org.lockss.util.rest.RestUtil;
import org.lockss.util.rest.multipart.MultipartMessage;
import org.lockss.util.rest.multipart.MultipartMessageHttpMessageConverter;
import org.lockss.ws.entities.ImportWsParams;
import org.lockss.ws.entities.ImportWsResult;
import org.lockss.ws.test.BaseSoapTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"security.basic.enabled=false"})
public class TestImportService extends BaseSoapTest {
  private static final L4JLogger log = L4JLogger.getLogger();

  private ImportService proxy;
  private static final String TARGET_NAMESPACE = "http://importer.ws.lockss.org/";
  private static final String SERVICE_NAME = "ImportServiceImplService";
  private static final String ENDPOINT_NAME = "ImportService";

  @Before
  public void init() throws Exception {
    setUpMultipartFormConverter();
    proxy = setUpProxyAndCommonTestEnv(TARGET_NAMESPACE,
                                       ENDPOINT_NAME, SERVICE_NAME,
                                       ImportService.class);
  }

  private static final byte[] HELLO_WORLD = "hello world".getBytes(StandardCharsets.UTF_8);
  private static final String IMPORT_CONTENT_PART_NAME = "file";

  /**
   * Test for {@link ImportService#importPulledFile(ImportWsParams)}.
   */
  @Test
  public void testImportPulledFile() throws Exception {
    String[] props = {"hello=world", "xyzzy=foobar"};

    ImportWsParams params = new ImportWsParams();
    params.setSourceUrl("https://www.lockss.org/hello-world.txt");
    params.setTargetId("targetId");
    params.setTargetUrl("targetUrl");
    params.setProperties(props);

    HttpHeaders srcHeaders = new HttpHeaders();
    srcHeaders.setContentType(MediaType.TEXT_PLAIN);
    srcHeaders.setContentLength(HELLO_WORLD.length);

    // Mock source URL fetch
    mockRestServer
        .expect(ExpectedCount.once(), requestTo(new URI(params.getSourceUrl())))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withStatus(HttpStatus.OK)
            .headers(srcHeaders)
            .body(HELLO_WORLD));

    // Prepare the endpoint URI
    String importEndpoint = getServiceEndpoint(ServiceDescr.SVC_POLLER) + "/ws/aus/import";
    URI importEndpointQuery = RestUtil.getRestUri(importEndpoint, null, null);

    // Mock REST service call and response
    mockRestServer
        .expect(ExpectedCount.once(), requestTo(importEndpointQuery))
        .andExpect(method(HttpMethod.PUT))
        .andExpect(header("Accept", "application/json"))
        .andExpect(header("Authorization", BASIC_AUTH_HASH))
        .andExpect(request -> {
          // Get multipart request headers and body
          HttpHeaders headers = request.getHeaders();
          String body = request.getBody().toString();

          // Construct new HttpInputMessage from request body
          HttpInputMessage inputMessage =
              new MockHttpInputMessage(body.getBytes(StandardCharsets.UTF_8));

          // Add request headers to input message
          inputMessage.getHeaders().putAll(headers);

          // Parse request body into multipart
          MultipartMessage mmsg =
              new MultipartMessageHttpMessageConverter().read(null, inputMessage);

          MultiValueMap<String, FileItem> parts = getRequestParts(mmsg);

          // Assert parts in multipart message
          assertIterableEquals(
              ListUtil.list("targetBaseUrlPath", "targetUrl", "userProperties", "file"),
              parts.keySet(),
              "Mis-matched parts in multipart import request");

          assertEquals("targetId", parts.getFirst("targetBaseUrlPath").getString());
          assertEquals("targetUrl", parts.getFirst("targetUrl").getString());

          assertTrue(MediaType.parseMediaType(parts.getFirst("userProperties").getContentType())
              .isCompatibleWith(MediaType.APPLICATION_JSON));
          assertEquals("[\"hello=world\",\"xyzzy=foobar\"]", parts.getFirst("userProperties").getString());

          assertTrue(MediaType.parseMediaType(parts.getFirst("file").getContentType())
              .isCompatibleWith(MediaType.TEXT_PLAIN));
          assertEquals(HELLO_WORLD, parts.getFirst("file").get());
        })
        .andRespond(withStatus(HttpStatus.OK));
//            .contentType(MediaType.APPLICATION_JSON));
//            .body(mapper.writeValueAsString(blankError)));

    // Make SOAP call
    ImportWsResult result = proxy.importPulledFile(params);

    // Assert successful import result
    assertNotNull(result);
    assertTrue(result.getIsSuccess());
    assertNull(result.getMessage());

    mockRestServer.verify();
    mockRestServer.reset();
  }

  private MultiValueMap<String, FileItem> getRequestParts(MultipartMessage mmsg) {
    MultiValueMap<String, FileItem> result = new LinkedMultiValueMap();
    for (int i = 0; i < mmsg.getCount(); i++) {
      FileItem item = mmsg.getPart(i);
      result.add(item.getFieldName(), item);
    }
    return result;
  }

  /**
   * Test for {@link ImportService#importPushedFile(ImportWsParams)}.
   */
  @Test
  public void testImportPushedFile() throws Exception {
    String[] props = {"hello=world", "xyzzy=foobar"};

    DataSource ds = new ByteArrayDataSource(HELLO_WORLD, "text/plain");
    DataHandler dh = new DataHandler(ds);

    ImportWsParams params = new ImportWsParams();
    params.setTargetId("targetId");
    params.setTargetUrl("targetUrl");
    params.setProperties(props);
    params.setDataHandler(dh);

    // Prepare the endpoint URI
    String importEndpoint = getServiceEndpoint(ServiceDescr.SVC_POLLER) + "/ws/aus/import";
    URI importEndpointQuery = RestUtil.getRestUri(importEndpoint, null, null);

    // Mock REST service call and response
    mockRestServer
        .expect(ExpectedCount.once(), requestTo(importEndpointQuery))
        .andExpect(method(HttpMethod.PUT))
        .andExpect(header("Accept", "application/json"))
        .andExpect(header("Authorization", BASIC_AUTH_HASH))
        .andExpect(request -> {
          // Get multipart request headers and body
          HttpHeaders headers = request.getHeaders();
          String body = request.getBody().toString();

          // Construct new HttpInputMessage from request body
          HttpInputMessage inputMessage =
              new MockHttpInputMessage(body.getBytes(StandardCharsets.UTF_8));

          // Add request headers to input message
          inputMessage.getHeaders().putAll(headers);

          // Parse request body into multipart
          MultipartMessage mmsg =
              new MultipartMessageHttpMessageConverter().read(null, inputMessage);

          MultiValueMap<String, FileItem> parts = getRequestParts(mmsg);

          // Assert parts in multipart message
          assertIterableEquals(
              ListUtil.list("targetBaseUrlPath", "targetUrl", "userProperties", "file"),
              parts.keySet(),
              "Mis-matched parts in multipart import request");

          assertEquals("targetId", parts.getFirst("targetBaseUrlPath").getString());
          assertEquals("targetUrl", parts.getFirst("targetUrl").getString());

          assertTrue(MediaType.parseMediaType(parts.getFirst("userProperties").getContentType())
              .isCompatibleWith(MediaType.APPLICATION_JSON));
          assertEquals("[\"hello=world\",\"xyzzy=foobar\"]", parts.getFirst("userProperties").getString());

          // FIXME: The Content-Type of DataHandler is stripped from the SOAP request by the SOAP used here.
          //  The Content-Type of the DataHandler received by the SOAP endpoint we're testing, is by default
          //  application/octet-stream. That default is then passed in the REST call, which we assert here:
          assertTrue(MediaType.parseMediaType(parts.getFirst("file").getContentType())
              // .isCompatibleWith(MediaType.TEXT_PLAIN));
              .isCompatibleWith(MediaType.APPLICATION_OCTET_STREAM));

          assertEquals(HELLO_WORLD, parts.getFirst("file").get());
        })
        .andRespond(withStatus(HttpStatus.OK));
//            .contentType(MediaType.APPLICATION_JSON));
//            .body(mapper.writeValueAsString(blankError)));

    // Make SOAP call
    ImportWsResult result = proxy.importPushedFile(params);

    // Assert successful import result
    assertNotNull(result);
    assertTrue(result.getIsSuccess());
    assertNull(result.getMessage());

    mockRestServer.verify();
    mockRestServer.reset();
  }

  /**
   * Test for {@link ImportService#getSupportedChecksumAlgorithms()}.
   */
  @Test
  public void testGetSupportedChecksumAlgorithms() throws Exception {
    List<String> supportedAlgorithms = ListUtil.list("A", "B", "C");

    // Prepare the endpoint URI
    String importEndpoint = getServiceEndpoint(ServiceDescr.SVC_REPO) + "/checksumalgorithms";
    URI importEndpointQuery = RestUtil.getRestUri(importEndpoint, null, null);

    // Mock REST service call and response
    mockRestServer
        .expect(ExpectedCount.once(), requestTo(importEndpointQuery))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("Authorization", BASIC_AUTH_HASH))
        .andRespond(withStatus(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(mapper.writeValueAsString(supportedAlgorithms)));

    String[] result = proxy.getSupportedChecksumAlgorithms();

    assertIterableEquals(supportedAlgorithms, Arrays.asList(result));

    mockRestServer.verify();
    mockRestServer.reset();
  }
}
