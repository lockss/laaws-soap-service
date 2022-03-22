package org.lockss.ws.hasher;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lockss.log.L4JLogger;
import org.lockss.spring.test.SpringLockssTestCase4;
import org.lockss.util.rest.RestResponseErrorBody;
import org.lockss.util.rest.RestUtil;
import org.lockss.util.rest.multipart.MultipartMessageHttpMessageConverter;
import org.lockss.ws.entities.HasherWsParams;
import org.lockss.ws.entities.HasherWsResult;
import org.lockss.ws.entities.LockssWebServicesFault;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lockss.ws.BaseServiceImpl.POLLER_SVC_URL_KEY;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"security.basic.enabled=false"})
public class TestHasherService extends SpringLockssTestCase4 {
  private static final L4JLogger log = L4JLogger.getLogger();

  @TestConfiguration
  public static class MyConfiguration {
    @Bean
    public RestTemplate restTemplate() {
      RestTemplate restTemplate = new RestTemplate();

      // Add multipart/form-data support
      List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters();
      messageConverters.add(new MultipartMessageHttpMessageConverter());

      return restTemplate;
    }
  }

  @Autowired
  protected Environment env;

  @Autowired
  private RestTemplate restTemplate;

  @LocalServerPort
  private int port;

  private HasherService proxy;
  private MockRestServiceServer mockRestServer;

  private static final ObjectMapper mapper = new ObjectMapper();

  private static final String TARGET_NAMESPACE = "http://hasher.ws.lockss.org/";
  private static final String SERVICE_NAME = "HasherServiceImplService";

  private static final String USERNAME = "lockss-u";
  private static final String PASSWORD = "lockss-p";
  private static final String BASIC_AUTH_HASH = "Basic bG9ja3NzLXU6bG9ja3NzLXA=";

  // FIXME: Blank mock REST error response
  private static final RestResponseErrorBody.RestResponseError blankError =
      new RestResponseErrorBody.RestResponseError();

  @Before
  public void init() throws MalformedURLException {
    // Setup proxy to SOAP service
    String wsdlEndpoint = "http://localhost:" + port + "/ws/HasherService?wsdl";
    Service srv = Service.create(new URL(wsdlEndpoint), new QName(TARGET_NAMESPACE, SERVICE_NAME));
    proxy = srv.getPort(HasherService.class);

    // Add authentication headers for SOAP request
    BindingProvider bp = (BindingProvider) proxy;
    Map<String, Object> requestContext = bp.getRequestContext();
    requestContext.put(BindingProvider.USERNAME_PROPERTY, USERNAME);
    requestContext.put(BindingProvider.PASSWORD_PROPERTY, PASSWORD);

    // Create MockRestServiceServer from RestTemplate
    mockRestServer = MockRestServiceServer.createServer(restTemplate);
  }

  @Test
  public void TestHash() throws Exception {

    //// Test bad or no auth error ("401 Unauthorized") handling
    {
      HasherWsParams params = new HasherWsParams();
      params.setAuId("auid1");

      Map<String, String> queryParams = new HashMap<>();
      queryParams.put("isAsynchronous", "false");

      // REST API endpoint of operation we're testing
      URI restEndpoint = RestUtil.getRestUri(
          env.getProperty(POLLER_SVC_URL_KEY) + "/hashes", null, queryParams);

      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.PUT))
          .andExpect(content().contentType("application/json;charset=UTF-8"))
          .andExpect(header("Accept", "multipart/form-data, application/json"))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andExpect(content().string(mapper.writeValueAsString(params)))
          .andRespond(withStatus(HttpStatus.UNAUTHORIZED)
              .contentType(MediaType.APPLICATION_JSON)
              .body(mapper.writeValueAsString(blankError)));

      // Make the call through SOAP
      assertThrows(LockssWebServicesFault.class,
          () -> proxy.hash(params),
          "401 Unauthorized");

      mockRestServer.verify();
      mockRestServer.reset();
    }

    //// Test bad User Role error ("Forbidden") handling
    {
      HasherWsParams params = new HasherWsParams();
      params.setAuId("auid1");

      Map<String, String> queryParams = new HashMap<>();
      queryParams.put("isAsynchronous", "false");

      // REST API endpoint of operation we're testing
      URI restEndpoint = RestUtil.getRestUri(
          env.getProperty(POLLER_SVC_URL_KEY) + "/hashes", null, queryParams);

      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.PUT))
          .andExpect(content().contentType("application/json;charset=UTF-8"))
          .andExpect(header("Accept", "multipart/form-data, application/json"))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andExpect(content().string(mapper.writeValueAsString(params)))
          .andRespond(withStatus(HttpStatus.FORBIDDEN)
              .contentType(MediaType.APPLICATION_JSON)
              .body(mapper.writeValueAsString(blankError)));

      // Make the call through SOAP
      assertThrows(LockssWebServicesFault.class,
          () -> proxy.hash(params),
          "403 Forbidden");

      mockRestServer.verify();
      mockRestServer.reset();
    }

    //// Test unforeseen exception ("500 Internal Server Error") handling
    {
      HasherWsParams params = new HasherWsParams();
      params.setAuId("auid1");

      Map<String, String> queryParams = new HashMap<>();
      queryParams.put("isAsynchronous", "false");

      // REST API endpoint of operation we're testing
      URI restEndpoint = RestUtil.getRestUri(
          env.getProperty(POLLER_SVC_URL_KEY) + "/hashes", null, queryParams);

      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.PUT))
          .andExpect(content().contentType("application/json;charset=UTF-8"))
          .andExpect(header("Accept", "multipart/form-data, application/json"))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andExpect(content().string(mapper.writeValueAsString(params)))
          .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
              .contentType(MediaType.APPLICATION_JSON)
              .body(mapper.writeValueAsString(blankError)));

      // Make the call through SOAP
      assertThrows(LockssWebServicesFault.class,
          () -> proxy.hash(params),
          "500 Internal Server Error");

      mockRestServer.verify();
      mockRestServer.reset();
    }

    //// Test success
    {
      HasherWsParams params = new HasherWsParams();
      params.setAuId("auid1");

      Map<String, String> queryParams = new HashMap<>();
      queryParams.put("isAsynchronous", "false");

      // REST API endpoint of operation we're testing
      URI restEndpoint = RestUtil.getRestUri(
          env.getProperty(POLLER_SVC_URL_KEY) + "/hashes", null, queryParams);

      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.PUT))
          .andExpect(content().contentType("application/json;charset=UTF-8"))
          .andExpect(header("Accept", "multipart/form-data, application/json"))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andExpect(content().string(mapper.writeValueAsString(params)))
          .andRespond(withStatus(HttpStatus.OK)
              .contentType(MediaType.MULTIPART_FORM_DATA)
              .body(mapper.writeValueAsString(blankError)));

      // Make the call through SOAP
      HasherWsResult result = proxy.hash(params);

      mockRestServer.verify();
      mockRestServer.reset();
    }
  }

}
