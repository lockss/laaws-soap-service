/*

 Copyright (c) 2015-2019 Board of Trustees of Leland Stanford Jr. University,
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
package org.lockss.ws.importer;

import org.lockss.log.L4JLogger;
import org.lockss.ws.entities.ImportWsParams;
import org.lockss.ws.entities.ImportWsResult;
import org.lockss.ws.entities.LockssWebServicesFault;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/**
 * The Import SOAP web service implementation.
 */
@Service
public class ImportServiceImpl implements ImportService {
  private final static L4JLogger log = L4JLogger.getLogger();

  @Autowired
  private Environment env;

  /**
   * Imports a pulled file into an archival unit.
   * 
   * @param importParams An ImportWsParams with the parameters of the importing
   *                     operation.
   * @return an ImportWsResult with the result of the operation.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public ImportWsResult importPulledFile(ImportWsParams importParams)
      throws LockssWebServicesFault {
    log.debug2("importParams = {}", importParams);

    try {
      // TODO: REPLACE THIS BLOCK WITH THE ACTUAL IMPLEMENTATION.
      // Prepare the result to be returned.
      ImportWsResult wsResult = new ImportWsResult();
      // TODO: END OF BLOCK TO BE REPLACED.

      log.debug2("wsResult = {}", wsResult);
      return wsResult;
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }

  /**
   * Imports a pushed file into an archival unit.
   * 
   * @param importParams An ImportWsParams with the parameters of the importing
   *                     operation.
   * @return an ImportWsResult with the result of the operation.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public ImportWsResult importPushedFile(ImportWsParams importParams)
      throws LockssWebServicesFault {
    log.debug2("importParams = {}", importParams);

    try {
      // TODO: REPLACE THIS BLOCK WITH THE ACTUAL IMPLEMENTATION.
      // Prepare the result to be returned.
      ImportWsResult wsResult = new ImportWsResult();
      // TODO: END OF BLOCK TO BE REPLACED.

      log.debug2("wsResult = {}", wsResult);
      return wsResult;
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }

  /**
   * Provides the names of the supported checksum algorithms.
   * 
   * @return a String[] with the names of the supported checksum algorithms.
   */
  @Override
  public String[] getSupportedChecksumAlgorithms() {
    log.debug2("Invoked.");

    // TODO: REPLACE THIS BLOCK WITH THE ACTUAL IMPLEMENTATION.
    String[] results = new String[1];
    results[0] = "Some Checksum Algorithm";
    // TODO: END OF BLOCK TO BE REPLACED.

    log.debug2("results = {}", (Object[])results);
    return results;
  }
}
