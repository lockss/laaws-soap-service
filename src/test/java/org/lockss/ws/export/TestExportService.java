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
package org.lockss.ws.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lockss.app.*;
import org.lockss.laaws.rs.util.NamedByteArrayResource;
import org.lockss.log.L4JLogger;
import org.lockss.spring.test.SpringLockssTestCase4;
import org.lockss.util.rest.RestResponseErrorBody;
import org.lockss.util.rest.RestUtil;
import org.lockss.util.rest.multipart.MultipartMessageHttpMessageConverter;
import org.lockss.ws.entities.DataHandlerWrapper;
import org.lockss.ws.entities.ExportServiceParams;
import org.lockss.ws.entities.ExportServiceWsResult;
import org.lockss.ws.test.BaseSoapTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"security.basic.enabled=false"})
public class TestExportService extends BaseSoapTest {
  private static final L4JLogger log = L4JLogger.getLogger();

  private ExportService proxy;

  private static final ObjectMapper mapper = new ObjectMapper();

  private static final String TARGET_NAMESPACE = "http://export.ws.lockss.org/";
  private static final String SERVICE_NAME = "ExportServiceImplService";
  private static final String ENDPOINT_NAME = "ExportService";

  public static String CONFIG_PART_NAME = "configFile";
  public static byte[] HELLO_WORLD = "hello world".getBytes(StandardCharsets.UTF_8);

  @Before
  public void init() throws Exception {

    setUpMultipartFormConverter();
    proxy = setUpProxyAndCommonTestEnv(TARGET_NAMESPACE,
                                       ENDPOINT_NAME, SERVICE_NAME,
                                       ExportService.class);
  }

  /**
   * Test for {@link ExportService#createExportFiles(ExportServiceParams)}.
   */
  @Test
  public void testCreateExportFiles() throws Exception {
    ExportServiceParams params = new ExportServiceParams();
    params.setAuid("testAuid");

    // Prepare the endpoint URI
    String endpointUri = getServiceEndpoint(ServiceDescr.SVC_POLLER) + "/aus/{auId}/export";

    // Prepare the URI path variables
    Map<String, String> uriVariables = new HashMap<>(1);
    uriVariables.put("auId", params.getAuid());

    // Prepare the query parameters
    Map<String, String> queryParams = new HashMap<>(1);
    queryParams.put("fileType", params.getFileType().toString());
    queryParams.put("isCompress", params.isCompress() ? "true" : "false");
    queryParams.put("isExcludeDirNodes", params.isExcludeDirNodes() ? "true" : "false");
    queryParams.put("xlateFilenames", params.getXlateFilenames().toString());
    queryParams.put("filePrefix", params.getFilePrefix());
    queryParams.put("maxSize", Long.valueOf(params.getMaxSize()).toString());
    queryParams.put("maxVersions", Integer.valueOf(params.getMaxVersions()).toString());
    log.trace("queryParams = {}", queryParams);

    URI createExportFilesQuery = RestUtil.getRestUri(endpointUri, uriVariables, queryParams);

    // Response part data
    Resource resource = new NamedByteArrayResource("test", HELLO_WORLD);

    // Response part headers
    HttpHeaders headers = new HttpHeaders();
    headers.setContentLength(HELLO_WORLD.length);

    // Parts of multipart response
    MultiValueMap parts = new LinkedMultiValueMap();
    parts.add(CONFIG_PART_NAME, new HttpEntity<>(resource, headers));

    // Write output message
    HttpOutputMessage outputMessage = new MockHttpOutputMessage();
    new AllEncompassingFormHttpMessageConverter()
        .write(parts, MediaType.MULTIPART_FORM_DATA, outputMessage);

    // Mock REST call for ArtifactData
    mockRestServer
        .expect(ExpectedCount.once(), requestTo(createExportFilesQuery))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("Accept", "multipart/form-data, application/json"))
        .andExpect(header("Authorization", BASIC_AUTH_HASH))
        .andRespond(withStatus(HttpStatus.OK)
            .headers(outputMessage.getHeaders())
            .body(outputMessage.getBody().toString()));

    ExportServiceWsResult result = proxy.createExportFiles(params);

    // Assert AUID matches
    assertEquals(params.getAuid(), result.getAuId());

    // Assert data matches
    DataHandlerWrapper[] wrappers = result.getDataHandlerWrappers();
    assertNotNull(wrappers);
    assertEquals(1, wrappers.length);
    assertInputStreamMatchesString("hello world",
        wrappers[0].getDataHandler().getInputStream());

    mockRestServer.verify();
    mockRestServer.reset();
  }

  // TODO: Test export of larger than 2GB
}
