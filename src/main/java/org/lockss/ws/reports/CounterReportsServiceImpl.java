/*

Copyright (c) 2013-2019 Board of Trustees of Leland Stanford Jr. University,
all rights reserved.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL
STANFORD UNIVERSITY BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

Except as contained in this notice, the name of Stanford University shall not
be used in advertising or otherwise to promote the sale, use or other dealings
in this Software without prior written authorization from Stanford University.

*/
package org.lockss.ws.reports;

import org.lockss.log.L4JLogger;
import org.lockss.ws.entities.CounterReportParams;
import org.lockss.ws.entities.CounterReportResult;
import org.lockss.ws.entities.LockssWebServicesFault;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import jakarta.xml.ws.soap.MTOM;

/** The COUNTER Reports SOAP web service implementation. */
@MTOM
@Service
public class CounterReportsServiceImpl implements CounterReportsService {
  private static final L4JLogger log = L4JLogger.getLogger();

  @Autowired private Environment env;

  /**
   * Provides a COUNTER report.
   *
   * @param reportParams A CounterReportParams with the parameters of the requested COUNTER report.
   * @return a CounterReportResult with the requested COUNTER report.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public CounterReportResult getCounterReport(CounterReportParams reportParams)
      throws LockssWebServicesFault {
    log.debug2("reportParams = {}", reportParams);

    try {
      // TODO: REPLACE THIS BLOCK WITH THE ACTUAL IMPLEMENTATION.
      CounterReportResult result = new CounterReportResult();
      // TODO: REPLACE THIS BLOCK WITH THE ACTUAL IMPLEMENTATION.

      log.debug2("result = {}", result);
      return result;
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }
}
