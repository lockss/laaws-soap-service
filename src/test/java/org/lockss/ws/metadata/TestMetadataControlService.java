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
package org.lockss.ws.metadata;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lockss.log.L4JLogger;
import org.lockss.ws.test.BaseSoapTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"security.basic.enabled=false"})
public class TestMetadataControlService extends BaseSoapTest {
  private static final L4JLogger log = L4JLogger.getLogger();

  private static final String TARGET_NAMESPACE = "http://metadata.ws.lockss.org/";
  private static final String SERVICE_NAME = "MetadataControlServiceImplService";
  private static final String ENDPOINT_NAME = "MetadataControlService";
  private MetadataControlService proxy;

  @Before
  public void init() throws Exception {
    proxy = setUpProxyAndCommonTestEnv(TARGET_NAMESPACE,
                                       ENDPOINT_NAME, SERVICE_NAME,
                                       MetadataControlService.class);
  }

  /**
   * Test for {@link MetadataControlService#deletePublicationIssn(Long, String, String)}.
   */
  @Test
  public void testDeletePublicationIssn() throws Exception {
    // TODO: Not implemented
  }

  /**
   * Test for {@link MetadataControlService#deleteAu(Long, String)}.
   */
  @Test
  public void testDeleteAu() throws Exception {
    // TODO: Not implemented
  }
}
