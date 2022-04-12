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
package org.lockss.ws.reports;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lockss.log.L4JLogger;
import org.lockss.util.rest.RestResponseErrorBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"security.basic.enabled=false"})
public class TestCounterReportsService {
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

  private CounterReportsService proxy;
  private MockRestServiceServer mockRestServer;

  private static final ObjectMapper mapper = new ObjectMapper();

  private static final String TARGET_NAMESPACE = "http://reports.ws.lockss.org/";
  private static final String SERVICE_NAME = "CounterReportsServiceImplService";

  private static final String USERNAME = "lockss-u";
  private static final String PASSWORD = "lockss-p";
  private static final String BASIC_AUTH_HASH = "Basic bG9ja3NzLXU6bG9ja3NzLXA=";

  // FIXME: Blank mock REST error response
  private static final RestResponseErrorBody.RestResponseError blankError =
      new RestResponseErrorBody.RestResponseError();

  @Before
  public void init() throws MalformedURLException {
    // Setup proxy to SOAP service
    String wsdlEndpoint = "http://localhost:" + port + "/ws/CounterReportsService?wsdl";
    Service srv = Service.create(new URL(wsdlEndpoint), new QName(TARGET_NAMESPACE, SERVICE_NAME));
    proxy = srv.getPort(CounterReportsService.class);

    // Add authentication headers for SOAP request
    BindingProvider bp = (BindingProvider) proxy;
    Map<String, Object> requestContext = bp.getRequestContext();
    requestContext.put(BindingProvider.USERNAME_PROPERTY, USERNAME);
    requestContext.put(BindingProvider.PASSWORD_PROPERTY, PASSWORD);

    // Create MockRestServiceServer from RestTemplate
    mockRestServer = MockRestServiceServer.createServer(restTemplate);
  }

  @Test
  public void test() throws Exception {

  }
}
