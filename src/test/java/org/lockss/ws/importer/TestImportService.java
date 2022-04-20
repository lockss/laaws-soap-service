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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.istack.ByteArrayDataSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lockss.laaws.rs.util.NamedInputStreamResource;
import org.lockss.log.L4JLogger;
import org.lockss.util.rest.RestResponseErrorBody;
import org.lockss.util.rest.RestUtil;
import org.lockss.ws.entities.ImportWsParams;
import org.lockss.ws.entities.ImportWsResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.lockss.ws.BaseServiceImpl.POLLER_SVC_URL_KEY;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"security.basic.enabled=false"})
public class TestImportService {
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

  private ImportService proxy;
  private MockRestServiceServer mockRestServer;

  private static final ObjectMapper mapper = new ObjectMapper();

  private static final String TARGET_NAMESPACE = "http://importer.ws.lockss.org/";
  private static final String SERVICE_NAME = "ImportServiceImplService";

  private static final String USERNAME = "lockss-u";
  private static final String PASSWORD = "lockss-p";
  private static final String BASIC_AUTH_HASH = "Basic bG9ja3NzLXU6bG9ja3NzLXA=";

  // FIXME: Blank mock REST error response
  private static final RestResponseErrorBody.RestResponseError blankError =
      new RestResponseErrorBody.RestResponseError();

  @Before
  public void init() throws MalformedURLException {
    // Setup proxy to SOAP service
    String wsdlEndpoint = "http://localhost:" + port + "/ws/ImportService?wsdl";
    Service srv = Service.create(new URL(wsdlEndpoint), new QName(TARGET_NAMESPACE, SERVICE_NAME));
    proxy = srv.getPort(ImportService.class);

    // Add authentication headers for SOAP request
    BindingProvider bp = (BindingProvider) proxy;
    Map<String, Object> requestContext = bp.getRequestContext();
    requestContext.put(BindingProvider.USERNAME_PROPERTY, USERNAME);
    requestContext.put(BindingProvider.PASSWORD_PROPERTY, PASSWORD);

    // Create MockRestServiceServer from RestTemplate
    mockRestServer = MockRestServiceServer.createServer(restTemplate);
  }

  private static final byte[] HELLO_WORLD = "hello world".getBytes(StandardCharsets.UTF_8);
  private static final String IMPORT_CONTENT_PART_NAME = "file";

  /**
   * Test for {@link ImportService#importPulledFile(ImportWsParams)}.
   */
  @Test
  public void testImportPulledFile() throws Exception {
    String[] props = {"hello", "world"};

    DataSource ds = new ByteArrayDataSource(HELLO_WORLD, "text/plain");
    DataHandler dh = new DataHandler(ds);

    ImportWsParams params = new ImportWsParams();
    params.setSourceUrl("sourceUrl");
    params.setTargetId("targetId");
    params.setTargetUrl("targetUrl");
    params.setProperties(props);
    params.setDataHandler(dh);

    // Prepare the endpoint URI
    String endpointUri = env.getProperty(POLLER_SVC_URL_KEY) + "/aus/import";
    URI uri = RestUtil.getRestUri(endpointUri, null, null);


    // Mock REST service call and response
    mockRestServer
        .expect(ExpectedCount.once(), requestTo(uri))
        .andExpect(method(HttpMethod.PUT))
        .andExpect(header("Accept", "application/json"))
        .andExpect(header("Authorization", BASIC_AUTH_HASH))
        .andExpect(content().contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
//        .andExpect(content().string(mapper.writeValueAsString(params)))
        .andExpect(content().formData(getRequestParts(params))
        .andRespond(withStatus(HttpStatus.UNAUTHORIZED)
            .contentType(MediaType.APPLICATION_JSON)
            .body(mapper.writeValueAsString(blankError)));

    ImportWsResult result = proxy.importPulledFile(params);
  }

  private MultiValueMap<String, Object> getRequestParts(ImportWsParams params) {
    MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
    parts.add("targetBaseUrlPath", params.getTargetId());
    parts.add("targetUrl", params.getTargetUrl());
    parts.add("userProperties", params.getProperties());

    Resource resource = new NamedInputStreamResource(IMPORT_CONTENT_PART_NAME, input);

    // Initialize the part headers
    HttpHeaders partHeaders = new HttpHeaders();
    partHeaders.setContentType(MediaType.TEXT_PLAIN);

    // This must be set or else AbstractResource#contentLength will read the
    // entire InputStream to determine the content length, which will exhaust
    // the InputStream.
    partHeaders.setContentLength(contentLength);

    parts.add(IMPORT_CONTENT_PART_NAME, new HttpEntity<>(resource, partHeaders));

    return parts;
  }

  /**
   * Test for {@link ImportService#importPushedFile(ImportWsParams)}.
   */
  @Test
  public void testImportPushedFile() throws Exception {
    // TODO
  }

  /**
   * Test for {@link ImportService#getSupportedChecksumAlgorithms()}.
   */
  @Test
  public void testGetSupportedChecksumAlgorithms() throws Exception {
    // TODO
  }
}
