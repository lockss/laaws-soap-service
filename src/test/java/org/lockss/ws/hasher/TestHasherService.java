package org.lockss.ws.hasher;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.cxf.attachment.AttachmentDataSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lockss.log.L4JLogger;
import org.lockss.spring.test.SpringLockssTestCase4;
import org.lockss.util.rest.RestResponseErrorBody;
import org.lockss.util.rest.RestUtil;
import org.lockss.util.rest.multipart.MultipartMessageHttpMessageConverter;
import org.lockss.ws.entities.HasherWsAsynchronousResult;
import org.lockss.ws.entities.HasherWsParams;
import org.lockss.ws.entities.HasherWsResult;
import org.lockss.ws.entities.LockssWebServicesFault;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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
  public void testHash() throws Exception {

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

  }

  @Test
  public void testHash_success() throws Exception {
    //// Test success
    {
      HasherWsParams params = new HasherWsParams();
      params.setAuId("auid1");

      Map<String, String> queryParams = new HashMap<>();
      queryParams.put("isAsynchronous", "false");

      // REST API endpoint of operation we're testing
      URI restEndpoint = RestUtil.getRestUri(
          env.getProperty(POLLER_SVC_URL_KEY) + "/hashes", null, queryParams);

      HasherWsAsynchronousResult hasherResult = new HasherWsAsynchronousResult();

      hasherResult.setStartTime(12345L);
      hasherResult.setRecordFileName("recordFileName");
      AttachmentDataSource recordSource = new AttachmentDataSource("text/plain",
          new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8)));
      DataHandler recordDataHandler = new DataHandler(recordSource);
//      hasherResult.setRecordFileDataHandler(new DataHandler(recordData));
      hasherResult.setBlockFileName("blockFileName");
      AttachmentDataSource blockSource = new AttachmentDataSource("text/plain",
          new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8)));
      DataHandler blockDataHandler = new DataHandler(blockSource);
//      hasherResult.setBlockFileDataHandler(new DataHandler(blockData));
      hasherResult.setHashResult("hashResult".getBytes(StandardCharsets.UTF_8));
      hasherResult.setErrorMessage("errorMsg");
      hasherResult.setStatus("testStatus");
      hasherResult.setBytesHashed(12345L);
      hasherResult.setFilesHashed(12345);
      hasherResult.setElapsedTime(12345L);
      hasherResult.setRequestTime(12345L);
      hasherResult.setRequestId("requestId");

      String responseBody = "--12345\r\n" +
          "Content-Disposition: form-data; name=\"testRequestId\"" + CRLF +
          "Content-Type: application/json" + CRLF +
          CRLF +
          mapper.writeValueAsString(hasherResult) + CRLF +
          "--12345\r\n" +
          "Content-Disposition: form-data; name=\"testRequestId-Block\"" + CRLF +
          "Content-Type: text/plain" + CRLF +
          CRLF +
          "test1" + CRLF +
          "--12345\r\n" +
          "Content-Disposition: form-data; name=\"testRequestId-Record\"" + CRLF +
          "Content-Type: text/plain" + CRLF +
          CRLF +
          "test2" + CRLF +
          "--12345--\r\n";

      HttpHeaders responseHeaders = new HttpHeaders();
      responseHeaders.setContentLength(responseBody.length());

      // Mock REST service call and response
      mockRestServer
          .expect(ExpectedCount.once(), requestTo(restEndpoint))
          .andExpect(method(HttpMethod.PUT))
          .andExpect(content().contentType("application/json;charset=UTF-8"))
          .andExpect(header("Accept", "multipart/form-data, application/json"))
          .andExpect(header("Authorization", BASIC_AUTH_HASH))
          .andExpect(content().string(mapper.writeValueAsString(params)))
          .andRespond(withStatus(HttpStatus.OK)
              .contentType(MediaType.parseMediaType("multipart/form-data; boundary=12345\n"))
              .headers(responseHeaders)
              .body(responseBody));

      // Make the call through SOAP
      HasherWsResult result = proxy.hash(params);

      assertEquals(hasherResult.getStartTime(), result.getStartTime());
      assertEquals(hasherResult.getRecordFileName(), result.getRecordFileName());
//      assertEquals(hasherResult.getRecordFileDataHandler(), result.getRecordFileDataHandler());
//      assertEquals(recordDataHandler.getContentType(), result.getRecordFileDataHandler().getContentType());
      assertInputStreamMatchesString("test2", result.getRecordFileDataHandler().getInputStream());;
      assertEquals(hasherResult.getBlockFileName(), result.getBlockFileName());
//      assertEquals(hasherResult.getBlockFileDataHandler(), result.getBlockFileDataHandler());
//      assertEquals(blockDataHandler.getContentType(), result.getBlockFileDataHandler().getContentType());
      assertInputStreamMatchesString("test1", result.getBlockFileDataHandler().getInputStream());;
      assertEquals(hasherResult.getHashResult(), result.getHashResult());
      assertEquals(hasherResult.getErrorMessage(), result.getErrorMessage());
      assertEquals(hasherResult.getStatus(), result.getStatus());
      assertEquals(hasherResult.getBytesHashed(), result.getBytesHashed());
      assertEquals(hasherResult.getFilesHashed(), result.getFilesHashed());
      assertEquals(hasherResult.getElapsedTime(), result.getElapsedTime());
//      assertEquals(hasherResult.getRequestTime(), result.getRequestTime());
//      assertEquals(hasherResult.getRequestId(), result.getRequestId());

      mockRestServer.verify();
      mockRestServer.reset();
    }
  }

  private static final String CRLF = "\r\n";
}
