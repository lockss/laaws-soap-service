package org.lockss.ws.content;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lockss.log.L4JLogger;
import org.lockss.spring.test.SpringLockssTestCase4;
import org.lockss.util.ListUtil;
import org.lockss.ws.entities.ContentConfigurationResult;
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
import javax.xml.ws.Service;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;

import static org.lockss.ws.BaseServiceImpl.CONFIG_SVC_URL_KEY;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"security.basic.enabled=false"})
public class TestContentConfigurationService extends SpringLockssTestCase4 {
  private static final L4JLogger log = L4JLogger.getLogger();

  @TestConfiguration
  public static class MyConfiguration {
    @Bean
    public RestTemplate restTemplate() {
      return new RestTemplate();
    }
  }

  @Autowired protected Environment env;
  @Autowired private RestTemplate restTemplate;

  @LocalServerPort
  private int port;

  private ContentConfigurationService proxy;
  private MockRestServiceServer mockRestServer;
  private ObjectMapper mapper = new ObjectMapper();

  private static final String TARGET_NAMESPACE = "http://content.ws.lockss.org/";
  private static final String SERVICE_NAME = "ContentConfigurationServiceImplService";

  @Before
  public void init() throws MalformedURLException {
    // Setup proxy to SOAP service
    String wsdlEndpoint = "http://localhost:" + port + "/ws/ContentConfigurationService?wsdl";
    Service srv = Service.create(new URL(wsdlEndpoint), new QName(TARGET_NAMESPACE, SERVICE_NAME));
    proxy = srv.getPort(ContentConfigurationService.class);

    // Create MockRestServiceServer from RestTemplate
    mockRestServer = MockRestServiceServer.createServer(restTemplate);
  }

  /**
   * Test for {@link ContentConfigurationService#addAuById(String)}.
   */
  @Test
  public void testAddAuById() throws Exception {
    // REST API endpoint of operation we're testing
    URI restEndpoint = new URI(env.getProperty(CONFIG_SVC_URL_KEY) + "/aus/add");

    // Mock REST response object
    ContentConfigurationResult expectedResult = new ContentConfigurationResult();
    expectedResult.setId("WOLF");

    List<String> auids = ListUtil.list("auid1");

    // Mock REST service call and response
    mockRestServer
        .expect(ExpectedCount.once(), requestTo(restEndpoint))
        .andExpect(method(HttpMethod.POST))
        .andExpect(content().contentType("application/json;charset=UTF-8"))
        .andExpect(content().string(mapper.writeValueAsString(auids)))
        .andRespond(withStatus(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(mapper.writeValueAsString(ListUtil.list(expectedResult))));

    // Make the call through SOAP
    ContentConfigurationResult result = proxy.addAuById("auid1");
    mockRestServer.verify();

    // assertEquals(expectedResult, result);
    assertEquals(expectedResult.getId(), result.getId());
    assertEquals(expectedResult.getName(), result.getName());
    assertEquals(expectedResult.getIsSuccess(), result.getIsSuccess());
    assertEquals(expectedResult.getMessage(), result.getMessage());
  }

  /**
   * Test for {@link ContentConfigurationService#addAusByIdList(java.util.List)}.
   *
   * @throws Exception
   */
  @Test
  public void testAddAusByIdList() throws Exception {
    // REST API endpoint of operation we're testing
    URI restEndpoint = new URI(env.getProperty(CONFIG_SVC_URL_KEY) + "/aus/add");

    // Mock REST response object
    ContentConfigurationResult expected1 = new ContentConfigurationResult();
    expected1.setId("WOLF1");

    ContentConfigurationResult expected2 = new ContentConfigurationResult();
    expected1.setId("WOLF2");

    List<String> auids = ListUtil.list("auid1", "auid2");

    // Mock REST service call and response
    mockRestServer
        .expect(ExpectedCount.once(), requestTo(restEndpoint))
        .andExpect(method(HttpMethod.POST))
        .andExpect(content().contentType("application/json;charset=UTF-8"))
        .andExpect(content().string(mapper.writeValueAsString(auids)))
        .andRespond(withStatus(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(mapper.writeValueAsString(ListUtil.list(expected1, expected2))));

    // Make the call through SOAP
    List<ContentConfigurationResult> results = proxy.addAusByIdList(auids);
    mockRestServer.verify();

    // assertEquals(expected1, result1);
    ContentConfigurationResult result1 = results.get(0);
    assertEquals(expected1.getId(), result1.getId());
    assertEquals(expected1.getName(), result1.getName());
    assertEquals(expected1.getIsSuccess(), result1.getIsSuccess());
    assertEquals(expected1.getMessage(), result1.getMessage());

    // assertEquals(expected2, result2);
    ContentConfigurationResult result2 = results.get(1);
    assertEquals(expected2.getId(), result2.getId());
    assertEquals(expected2.getName(), result2.getName());
    assertEquals(expected2.getIsSuccess(), result2.getIsSuccess());
    assertEquals(expected2.getMessage(), result2.getMessage());
  }

}
