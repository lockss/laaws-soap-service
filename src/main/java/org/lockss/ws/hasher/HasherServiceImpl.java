/*

 Copyright (c) 2014-2019 Board of Trustees of Leland Stanford Jr. University,
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
package org.lockss.ws.hasher;

import java.util.ArrayList;
import java.util.List;
import org.lockss.log.L4JLogger;
import org.lockss.ws.entities.HasherWsAsynchronousResult;
import org.lockss.ws.entities.HasherWsParams;
import org.lockss.ws.entities.HasherWsResult;
import org.lockss.ws.entities.LockssWebServicesFault;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/**
 * The Hasher SOAP web service implementation.
 */
@Service
public class HasherServiceImpl implements HasherService {
  private final static L4JLogger log = L4JLogger.getLogger();

  @Autowired
  private Environment env;

  /**
   * Performs the hashing of an AU or a URL.
   * 
   * @param wsParams A HasherWsParams with the parameters of the hashing
   *                 operation.
   * @return a HasherWsResult with the result of the hashing operation.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public HasherWsResult hash(HasherWsParams wsParams)
      throws LockssWebServicesFault {
    log.debug2("wsParams = {}", wsParams);

    try {
      // TODO: REPLACE THIS BLOCK WITH THE ACTUAL IMPLEMENTATION.
      HasherWsResult result = new HasherWsResult();
      // TODO: END OF BLOCK TO BE REPLACED.

      log.debug2("result = {}", result);
      return result;
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }

  /**
   * Performs asynchronously the hashing of an AU or a URL.
   * 
   * @param wsParams A HasherWsParams with the parameters of the hashing
   *                 operation.
   * @return a HasherWsAsynchronousResult with the result of the hashing
   *         operation.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public HasherWsAsynchronousResult hashAsynchronously(HasherWsParams wsParams)
      throws LockssWebServicesFault {
    log.debug2("wsParams = {}", wsParams);

    try {
      // TODO: REPLACE THIS BLOCK WITH THE ACTUAL IMPLEMENTATION.
      HasherWsAsynchronousResult result = new HasherWsAsynchronousResult();
      // TODO: END OF BLOCK TO BE REPLACED.

      log.debug2("result = {}", result);
      return result;
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }

  /**
   * Provides the result of an asynchronous hashing operation.
   * 
   * @param requestId A String with the identifier of the requested asynchronous
   *                  hashing operation.
   * @return a HasherWsAsynchronousResult with the result of the hashing
   *         operation.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public HasherWsAsynchronousResult getAsynchronousHashResult(String requestId)
      throws LockssWebServicesFault {
    log.debug2("requestId = {}", requestId);

    try {
      // TODO: REPLACE THIS BLOCK WITH THE ACTUAL IMPLEMENTATION.
      HasherWsAsynchronousResult result = new HasherWsAsynchronousResult();
      // TODO: END OF BLOCK TO BE REPLACED.

      log.debug2("result = {}", result);
      return result;
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }

  /**
   * Provides the results of all the asynchronous hashing operations.
   * 
   * @return a {@code List<HasherWsAsynchronousResult>} with the results of the
   *         hashing operations.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public List<HasherWsAsynchronousResult> getAllAsynchronousHashResults()
      throws LockssWebServicesFault {
    log.debug2("Invoked.");

    try {
      // TODO: REPLACE THIS BLOCK WITH THE ACTUAL IMPLEMENTATION.
      // Initialize the response.
      List<HasherWsAsynchronousResult> wsResults =
	  new ArrayList<HasherWsAsynchronousResult>();
      // TODO: END OF BLOCK TO BE REPLACED.

      log.debug2("wsResults = {}", wsResults);
      return wsResults;
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }

  /**
   * Removes from the system an asynchronous hashing operation, terminating it
   * if it's still running.
   * 
   * @param requestId A String with the identifier of the requested asynchronous
   *                  hashing operation.
   * @return a HasherWsAsynchronousResult with the result of the removal of the
   *         hashing operation.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public HasherWsAsynchronousResult removeAsynchronousHashRequest(String
      requestId) throws LockssWebServicesFault {
    log.debug2("requestId = {}", requestId);

    try {
      // TODO: REPLACE THIS BLOCK WITH THE ACTUAL IMPLEMENTATION.
      HasherWsAsynchronousResult result = new HasherWsAsynchronousResult();
      // TODO: END OF BLOCK TO BE REPLACED.

      log.debug2("result = {}", result);
      return result;
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }
}
