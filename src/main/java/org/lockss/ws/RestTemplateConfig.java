package org.lockss.ws;

import org.apache.commons.io.FileUtils;
import org.lockss.util.Constants;
import org.lockss.util.rest.RestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestTemplate;

import java.io.File;

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
    return RestUtil.getRestTemplate(getConnectionTimeout(), getReadTimeout(),
        getSizeThreshold(), getTmpDir());
  }

  /** The configuration key for the connection timeout. */
  public static final String CONNECTION_TIMEOUT_KEY = "connection.timeout";

  /** The configuration key for the read timeout. */
  public static final String READ_TIMEOUT_KEY = "read.timeout";

  /** Response body size threshold key. */
   public static final String SIZE_THRESHOLD_KEY = "size.threshold";

   /** Key for path to temporary directory */
   public static final String TMP_DIR_KEY = "tmp.dir";

  // Default timeouts.
  private final long defaultConnectTimeout = 10 * Constants.SECOND;
  private final long defaultReadTimeout = 120 * Constants.SECOND;

  private final int DEFAULT_SIZE_THRESHOLD = 16 * (int)FileUtils.ONE_MB;
  private final File DEFAULT_TMP_DIR = null;

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

  protected int getSizeThreshold() {
    return env.getProperty(SIZE_THRESHOLD_KEY, Integer.class, DEFAULT_SIZE_THRESHOLD);
  }

  protected File getTmpDir() {
    String val = env.getProperty(TMP_DIR_KEY, String.class);
    return (val == null) ? null : new File(val);
  }
}
