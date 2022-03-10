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
package org.lockss.ws.cxf;

import org.apache.cxf.Bus;
import org.apache.cxf.annotations.Provider;
import org.apache.cxf.annotations.Provider.Type;
import org.apache.cxf.common.injection.NoJSR250Annotations;
import org.apache.cxf.ext.logging.LoggingInInterceptor;
import org.apache.cxf.ext.logging.LoggingOutInterceptor;
import org.apache.cxf.ext.logging.event.LogEventSender;
import org.apache.cxf.ext.logging.event.PrettyLoggingFilter;
import org.apache.cxf.feature.AbstractFeature;
import org.apache.cxf.interceptor.InterceptorProvider;

/**
 * This class is used to control message-on-the-wire logging at log level TRACE instead of the
 * default log level INFO of org.apache.cxf.ext.logging.LoggingFeature. Usage:
 *
 * <pre>
 * <![CDATA[
 * <jaxws:endpoint ...>
 * <jaxws:features>
 * <bean class="org.lockss.ws.cxf.LockssLoggingFeature"/>
 * </jaxws:features>
 * </jaxws:endpoint>
 * ]]>
 * </pre>
 */
@NoJSR250Annotations
@Provider(value = Type.Feature)
public class LockssLoggingFeature extends AbstractFeature {
  private final LoggingInInterceptor in;
  private final LoggingOutInterceptor out;
  private final PrettyLoggingFilter inPrettyFilter;
  private final PrettyLoggingFilter outPrettyFilter;

  /** Default constructor. */
  public LockssLoggingFeature() {
    LogEventSender sender = new LockssSlf4jVerboseEventSender();
    inPrettyFilter = new PrettyLoggingFilter(sender);
    outPrettyFilter = new PrettyLoggingFilter(sender);
    setPrettyLogging(true);
    in = new LoggingInInterceptor(inPrettyFilter);
    out = new LoggingOutInterceptor(outPrettyFilter);
  }

  @Override
  protected void initializeProvider(InterceptorProvider provider, Bus bus) {
    provider.getInInterceptors().add(in);
    provider.getInFaultInterceptors().add(in);

    provider.getOutInterceptors().add(out);
    provider.getOutFaultInterceptors().add(out);
  }

  public void setLimit(int limit) {
    in.setLimit(limit);
    out.setLimit(limit);
  }

  public void setInMemThreshold(long inMemThreshold) {
    in.setInMemThreshold(inMemThreshold);
    out.setInMemThreshold(inMemThreshold);
  }

  public void setSender(LogEventSender sender) {
    this.inPrettyFilter.setNext(sender);
    this.outPrettyFilter.setNext(sender);
  }

  public void setInSender(LogEventSender s) {
    this.inPrettyFilter.setNext(s);
  }

  public void setOutSender(LogEventSender s) {
    this.outPrettyFilter.setNext(s);
  }

  public void setPrettyLogging(boolean prettyLogging) {
    this.inPrettyFilter.setPrettyLogging(prettyLogging);
    this.outPrettyFilter.setPrettyLogging(prettyLogging);
  }

  /**
   * Log binary content?
   *
   * @param logBinary defaults to false
   */
  public void setLogBinary(boolean logBinary) {
    in.setLogBinary(logBinary);
    out.setLogBinary(logBinary);
  }

  /**
   * Log multipart content?
   *
   * @param logMultipart defaults to true
   */
  public void setLogMultipart(boolean logMultipart) {
    in.setLogMultipart(logMultipart);
    out.setLogMultipart(logMultipart);
  }

  public void setVerbose(boolean verbose) {
    setSender(verbose ? new LockssSlf4jVerboseEventSender() : new LockssSlf4jEventSender());
  }
}
