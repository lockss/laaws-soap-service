package org.lockss.ws;

import org.lockss.util.Constants;
import org.lockss.util.rest.RestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestTemplate;

/**
 * Configures and provides a {@link RestTemplate} bean for use within the SOAP Service's
 * Spring context. The {@link RestTemplate} is {@link Autowired} into the SOAP Service's
 * various service implementations through the base class {@link BaseServiceImpl}. It is
 * used to forward SOAP calls to external REST services.
 *
 * {@link RestTemplate} is thread-safe.
 */
@Configuration
public class RestTemplateConfig {

  @Autowired protected Environment env;

  /**
   * Provides the customized template used by Spring for synchronous client-side HTTP access.
   *
   * @return a RestTemplate with the customized Spring template.
   */
  @Bean
  protected RestTemplate restTemplate() {
    return RestUtil.getRestTemplate(getConnectionTimeout(), getReadTimeout());
  }

  /** The configuration key for the connection timeout. */
  public static final String CONNECTION_TIMEOUT_KEY = "connection.timeout";

  /** The configuration key for the read timeout. */
  public static final String READ_TIMEOUT_KEY = "read.timeout";

  // Default timeouts.
  private final long defaultConnectTimeout = 10 * Constants.SECOND;
  private final long defaultReadTimeout = 120 * Constants.SECOND;

  /**
   * Provides the configured connection timeout in milliseconds.
   *
   * @return a Long with the configured connection timeout in milliseconds.
   */
  protected Long getConnectionTimeout() {
    return env.getProperty(CONNECTION_TIMEOUT_KEY, Long.class, defaultConnectTimeout);
  }

  /**
   * Provides the configured read timeout in milliseconds.
   *
   * @return a Long with the configured read timeout in milliseconds.
   */
  protected Long getReadTimeout() {
    return env.getProperty(READ_TIMEOUT_KEY, Long.class, defaultReadTimeout);
  }

}
