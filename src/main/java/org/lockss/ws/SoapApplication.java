/*

Copyright (c) 2000-2019 Board of Trustees of Leland Stanford Jr. University,
all rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice,
this list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

3. Neither the name of the copyright holder nor the names of its contributors
may be used to endorse or promote products derived from this software without
specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 */
package org.lockss.ws;

import org.lockss.app.LockssApp;
import org.lockss.app.LockssApp.AppSpec;
import org.lockss.app.LockssDaemon;
import org.lockss.app.ServiceDescr;
import org.lockss.log.L4JLogger;
import org.lockss.plugin.PluginManager;
import org.lockss.spring.base.BaseSpringBootApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.solr.SolrAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ImportResource;

import static org.lockss.app.LockssApp.PARAM_START_PLUGINS;
import static org.lockss.app.ManagerDescs.ACCOUNT_MANAGER_DESC;

/** Launcher of the Spring Boot application. */
@SpringBootApplication(exclude = {SolrAutoConfiguration.class})
//@SpringBootApplication
@ImportResource({"classpath:webservice-definition-beans.xml"})
public class SoapApplication implements CommandLineRunner {
  private static L4JLogger log = L4JLogger.getLogger();

  @Autowired
  private ApplicationContext appCtx;

  // Manager descriptors.  The order of this table determines the order in
  // which managers are initialized and started.
  private static final LockssApp.ManagerDesc[] myManagerDescs = {
      ACCOUNT_MANAGER_DESC
  };

  /**
   * The entry point of the application.
   *
   * @param args A String[] with the command line arguments.
   */
  public static void main(String[] args) {
    log.info("Starting the application");
//     configure();

    SpringApplication.run(SoapApplication.class, args);
  }

  /**
   * Callback used to run the application starting the LOCKSS daemon.
   *
   * @param args
   *          A String[] with the command line arguments.
   */
  public void run(String... args) {
    // Check whether there are command line arguments available.
     if (args != null && args.length > 0) {
      // Yes: Start the LOCKSS daemon.
      log.info("Starting the LOCKSS SOAP Service");

      AppSpec spec = new AppSpec()
          .setService(ServiceDescr.SVC_SOAP)
        .setName("SOAP Service")
          .setArgs(args)
          .addAppConfig(PARAM_START_PLUGINS, "false")
          .addAppDefault(PluginManager.PARAM_START_ALL_AUS, "false")
          .setSpringApplicatonContext(appCtx)
          .setAppManagers(myManagerDescs);

      LockssApp.startStatic(LockssDaemon.class, spec);
    } else {
      // No: Do nothing. This happens when a test is started and before the
      // test setup has got a chance to inject the appropriate command line
      // parameters.
    }
  }
}
