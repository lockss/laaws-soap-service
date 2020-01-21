/*

Copyright (c) 2000-2020 Board of Trustees of Leland Stanford Jr. University,
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

import java.io.IOException;
import java.util.List;
import java.util.Vector;
import org.lockss.log.L4JLogger;
import org.lockss.util.ListUtil;
import org.lockss.util.io.FileUtil;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

/**
 * Launcher of the Spring Boot application.
 */
@SpringBootApplication
@ImportResource({"classpath:webservice-definition-beans.xml"})
public class SoapApplication implements CommandLineRunner {
  private final static L4JLogger log = L4JLogger.getLogger();
  private final static String OPTION_REST_CLIENT_CREDENTIALS_FILE_PATH = "-r";
  private static List<String> restClientCredentials = null;

  /**
   * The entry point of the application.
   *
   * @param args A String[] with the command line arguments.
   */
  public static void main(String[] args) {
    SpringApplication.run(SoapApplication.class, args);
  }

  /**
   * Callback used to run the application.
   *
   * @param args
   *          A String[] with the command line arguments.
   */
  public void run(String... args) {
    log.debug2("args = {}", ListUtil.fromArray(args));

    // Loop through all the command line options.
    for (int i = 0; i < args.length; i++) {
      // Check whether this is the REST client credentials file path option.
      if (args[i].equals(OPTION_REST_CLIENT_CREDENTIALS_FILE_PATH)
	  && i < args.length - 1) {
	// Yes: Get the REST client credentials file path.
	String restClientCredentialsFilePath = args[++i];
	log.trace("credentialsFilePath = {}", restClientCredentialsFilePath);

	// Check whether a file path for the credentials has been specified.
	if (restClientCredentialsFilePath != null) {
	  // Yes.
	  try {
	    // Read the credentials from the file.
	    String credentials =
		FileUtil.readPasswdFile(restClientCredentialsFilePath);

	    // Parse the credentials.
	    if (credentials != null && !credentials.isEmpty()) {
	      // TODO: Replace with a call to the appropriate method in
	      // StringUtil once StringUtil has been moved from the lockss-core
	      // project to the lockss-util project.
	      restClientCredentials = breakAt(credentials, ":");
	    }
	  } catch (IOException ioe) {
	    log.warn("Exception caught getting REST client credentials", ioe);
	  }
	}
      }
    }
  }

  /**
   * Provides the credentials to be used when calling REST services.
   * 
   * @return a List<String> with the credentials.
   */
  public static List<String> getRestClientCredentials() {
    return restClientCredentials;
  }

  /**
   * Breaks a string at a separator string.
   * 
   * @param s   A String containing zero or more occurrences of the separator
   *            string.
   * @param sep A String with the separator string.
   */
  // TODO: Remove the method below (mostly copied from StringUtil) once
  // StringUtil has been moved from the lockss-core project to the lockss-util
  // project.
  private static Vector<String> breakAt(String s, String sep) {
    Vector<String> res = new Vector<>();
    int len;
    if (s == null || (len = s.length()) == 0) {
      return res;
    }
    int maxItems = Integer.MAX_VALUE;
    for (int pos = 0; maxItems > 0; maxItems-- ) {
      int end = s.indexOf(sep, pos);
      if (end == -1) {
	if (pos > len) {
	  break;
	}
	end = len;
      }
      if (pos != end) {
	String str = s.substring(pos, end);
	str = str.trim();
	if (str.length() != 0) {
	  res.addElement(str);
	}
      }
      pos = end + sep.length();
    }
    return res;
  }
}
