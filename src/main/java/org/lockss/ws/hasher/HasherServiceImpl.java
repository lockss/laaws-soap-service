/*

Copyright (c) 2014-2020 Board of Trustees of Leland Stanford Jr. University,
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

import org.lockss.app.ServiceDescr;
import org.lockss.log.L4JLogger;
import org.lockss.util.Constants;
import org.lockss.util.rest.poller.RestPollerClient;
import org.lockss.ws.BaseServiceImpl;
import org.lockss.ws.entities.HasherWsAsynchronousResult;
import org.lockss.ws.entities.HasherWsParams;
import org.lockss.ws.entities.HasherWsResult;
import org.lockss.ws.entities.LockssWebServicesFault;
import org.springframework.stereotype.Service;

import java.util.List;

/** The Hasher SOAP web service implementation. */
@Service
public class HasherServiceImpl extends BaseServiceImpl implements HasherService {
  private static final L4JLogger log = L4JLogger.getLogger();

  /**
   * Performs the hashing of an AU or a URL.
   *
   * @param wsParams A HasherWsParams with the parameters of the hashing operation.
   * @return a HasherWsResult with the result of the hashing operation.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public HasherWsResult hash(HasherWsParams wsParams) throws LockssWebServicesFault {
    log.debug2("wsParams = {}", wsParams);

    try {
      // Make the REST call to perform the hash.
      HasherWsResult result =
          new RestPollerClient(getServiceEndpoint(ServiceDescr.SVC_POLLER))
              .addRequestHeaders(getAuthHeaders())
              .setTimeouts(10 * Constants.SECOND, Constants.DAY)
              .setRestTemplate(restTemplate)
              .hash(wsParams);

      log.debug2("result = {}", result);
      return result;
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }

  /**
   * Performs asynchronously the hashing of an AU or a URL.
   *
   * @param wsParams A HasherWsParams with the parameters of the hashing operation.
   * @return a HasherWsAsynchronousResult with the result of the hashing operation.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public HasherWsAsynchronousResult hashAsynchronously(HasherWsParams wsParams)
      throws LockssWebServicesFault {
    log.debug2("wsParams = {}", wsParams);

    try {
      // Make the REST call to perform the hash.
      HasherWsAsynchronousResult result =
          new RestPollerClient(getServiceEndpoint(ServiceDescr.SVC_POLLER))
              .addRequestHeaders(getAuthHeaders())
              .setRestTemplate(restTemplate)
              .hashAsynchronously(wsParams);

      log.debug2("result = {}", result);
      return result;
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }

  /**
   * Provides the result of an asynchronous hashing operation.
   *
   * @param requestId A String with the identifier of the requested asynchronous hashing operation.
   * @return a HasherWsAsynchronousResult with the result of the hashing operation.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public HasherWsAsynchronousResult getAsynchronousHashResult(String requestId)
      throws LockssWebServicesFault {
    log.debug2("requestId = {}", requestId);

    try {
      // Make the REST call to get the hash.
      HasherWsAsynchronousResult result =
          new RestPollerClient(getServiceEndpoint(ServiceDescr.SVC_POLLER))
              .addRequestHeaders(getAuthHeaders())
              .setRestTemplate(restTemplate)
              .getAsynchronousHashResult(requestId);

      log.debug2("result = {}", result);
      return result;
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }

  /**
   * Provides the results of all the asynchronous hashing operations.
   *
   * @return a {@code List<HasherWsAsynchronousResult>} with the results of the hashing operations.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public List<HasherWsAsynchronousResult> getAllAsynchronousHashResults()
      throws LockssWebServicesFault {
    log.debug2("Invoked.");

    try {
      // Make the REST call to get all the hashes.
      List<HasherWsAsynchronousResult> wsResults =
          new RestPollerClient(getServiceEndpoint(ServiceDescr.SVC_POLLER))
              .addRequestHeaders(getAuthHeaders())
              .setRestTemplate(restTemplate)
              .getAllAsynchronousHashResults();

      log.debug2("wsResults = {}", wsResults);
      return wsResults;
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }

  /**
   * Removes from the system an asynchronous hashing operation, terminating it if it's still
   * running.
   *
   * @param requestId A String with the identifier of the requested asynchronous hashing operation.
   * @return a HasherWsAsynchronousResult with the result of the removal of the hashing operation.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public HasherWsAsynchronousResult removeAsynchronousHashRequest(String requestId)
      throws LockssWebServicesFault {
    log.debug2("requestId = {}", requestId);

    try {
      // Make the REST call to remove the hash.
      HasherWsAsynchronousResult result =
          new RestPollerClient(getServiceEndpoint(ServiceDescr.SVC_POLLER))
              .addRequestHeaders(getAuthHeaders())
              .setRestTemplate(restTemplate)
              .removeAsynchronousHashRequest(requestId);

      log.debug2("result = {}", result);
      return result;
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }
}
