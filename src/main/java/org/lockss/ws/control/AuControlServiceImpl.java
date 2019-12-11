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
package org.lockss.ws.control;

import java.util.ArrayList;
import java.util.List;
import org.lockss.log.L4JLogger;
import org.lockss.ws.entities.CheckSubstanceResult;
import org.lockss.ws.entities.LockssWebServicesFault;
import org.lockss.ws.entities.RequestCrawlResult;
import org.lockss.ws.entities.RequestDeepCrawlResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.lockss.ws.entities.RequestAuControlResult;

/**
 * The AU Control SOAP web service implementation.
 */
@Service
public class AuControlServiceImpl implements AuControlService {
  static final String MISSING_AU_ID_ERROR_MESSAGE = "Missing auId";
  static final String NO_SUBSTANCE_ERROR_MESSAGE =
      "No substance patterns defined for plugin";
  static final String NO_SUCH_AU_ERROR_MESSAGE = "No such Archival Unit";
  static final String UNEXPECTED_SUBSTANCE_CHECKER_ERROR_MESSAGE =
      "Error in SubstanceChecker; see log";
  static final String USE_FORCE_MESSAGE =
      "Use the 'force' parameter to override.";
  static final String DISABLED_METADATA_PROCESSING_ERROR_MESSAGE =
      "Metadata processing is not enabled";
  static final String DISABLE_METADATA_INDEXING_ERROR_MESSAGE =
      "Cannot disable AU metadata indexing";
  static final String ACTION_ENABLE_METADATA_INDEXING = "Enable Indexing";
  static final String ENABLE_METADATA_INDEXING_ERROR_MESSAGE =
      "Cannot enable AU metadata indexing";

  private final static L4JLogger log = L4JLogger.getLogger();

  @Autowired
  private Environment env;

  /**
   * Provides an indication of whether an Archival Unit has substance.
   * 
   * @param auId A String with the identifier (auid) of the archival unit.
   * @return a CheckSubstanceResult with the result of the operation.
   * @throws LockssWebServicesFault if there are problems.
   */
  public CheckSubstanceResult checkSubstanceById(String auId)
      throws LockssWebServicesFault {
    log.debug2("auId = {}", auId);

    try {
      // TODO: REPLACE THIS BLOCK WITH THE ACTUAL IMPLEMENTATION.
      CheckSubstanceResult result =
	  new CheckSubstanceResult(auId, null, null, "It worked!");
      // TODO: END OF BLOCK TO BE REPLACED.

      log.debug2("result = {}", result);
      return result;
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }

  /**
   * Provides an indication of whether the archival units defined by a list with
   * their identifiers have substance.
   * 
   * @param auIds A {@code List<String>} with the identifiers (auids) of the
   *              archival units.
   * @return a {@code List<CheckSubstanceResult>} with the results of the
   *         operation.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public List<CheckSubstanceResult> checkSubstanceByIdList(List<String> auIds)
      throws LockssWebServicesFault {
    log.debug2("auIds = {}", auIds);

    List<CheckSubstanceResult> results =
	new ArrayList<CheckSubstanceResult>(auIds.size());

    // Loop through all the Archival Unit identifiers.
    for (String auId : auIds) {
      results.add(checkSubstanceById(auId));
    }

    log.debug2("results = {}", results);
    return results;
  }

  /**
   * Requests the crawl of an archival unit.
   * 
   * @param auId     A String with the identifier (auid) of the archival unit.
   * @param priority An Integer with the priority of the crawl request.
   * @param force    A boolean with <code>true</code> if the request is to be
   *                 made even in the presence of some anomalies,
   *                 <code>false</code> otherwise.
   * @return a RequestCrawlResult with the result of the operation.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public RequestCrawlResult requestCrawlById(String auId, Integer priority,
      boolean force) throws LockssWebServicesFault {
    log.debug2("auId = {}, priority = {}, force = {}", auId, priority, force);

    try {
      // TODO: REPLACE THIS BLOCK WITH THE ACTUAL IMPLEMENTATION.
      RequestCrawlResult result =
	  new RequestCrawlResult(auId, true, "No delay", "It worked!");
      // TODO: END OF BLOCK TO BE REPLACED.

      log.debug2("result = {}", result);
      return result;
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }

  /**
   * Requests the crawl of the archival units defined by a list with their
   * identifiers.
   * 
   * @param auIds    A {@code List<String>} with the identifiers (auids) of the
   *                 archival units.
   * @param priority An Integer with the priority of the crawl request.
   * @param force    A boolean with <code>true</code> if the request is to be
   *                 made even in the presence of some anomalies,
   *                 <code>false</code> otherwise.
   * @return a {@code List<RequestCrawlResult>} with the results of the
   *         operation.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public List<RequestCrawlResult> requestCrawlByIdList(List<String> auIds,
      Integer priority, boolean force) throws LockssWebServicesFault {
    log.debug2("auIds = {}, priority = {}, force = {}", auIds, priority, force);

    List<RequestCrawlResult> results =
	new ArrayList<RequestCrawlResult>(auIds.size());

    // Loop through all the Archival Unit identifiers.
    for (String auId : auIds) {
      // Perform the request.
      results.add(requestCrawlById(auId, priority, force));
    }

    log.debug2("results = {}", results);
    return results;
  }

  /**
   * Requests the deep crawl of an archival unit.
   * 
   * @param auId         A String with the identifier (auid) of the archival
   *                     unit.
   * @param refetchDepth An int with the depth of the crawl request.
   * @param priority     An Integer with the priority of the crawl request.
   * @param force        A boolean with <code>true</code> if the request is to
   *                     be made even in the presence of some anomalies,
   *                     <code>false</code> otherwise.
   * @return a RequestDeepCrawlResult with the result of the operation.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public RequestDeepCrawlResult requestDeepCrawlById(String auId,
      int refetchDepth, Integer priority, boolean force)
	  throws LockssWebServicesFault {
    log.debug2("auId = {}, refetchDepth = {}, priority = {}, force = {}", auId,
	refetchDepth, priority, force);

    try {
      // TODO: REPLACE THIS BLOCK WITH THE ACTUAL IMPLEMENTATION.
      RequestDeepCrawlResult result =
	  new RequestDeepCrawlResult(auId, 1, true, "No delay", "It worked!");
      // TODO: END OF BLOCK TO BE REPLACED.

      log.debug2("result = {}", result);
      return result;
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }

  /**
   * Requests the deep crawl of the archival units defined by a list with their
   * identifiers.
   * 
   * @param auIds        A {@code List<String>} with the identifiers (auids) of
   *                     the archival units.
   * @param refetchDepth An int with the depth of the crawl request.
   * @param priority     An Integer with the priority of the crawl request.
   * @param force        A boolean with <code>true</code> if the request is to
   *                     be made even in the presence of some anomalies,
   *                     <code>false</code> otherwise.
   * @return a {@code List<RequestDeepCrawlResult>} with the results of the
   *         operation.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public List<RequestDeepCrawlResult> requestDeepCrawlByIdList(
      List<String> auIds, int refetchDepth, Integer priority, boolean force)
	  throws LockssWebServicesFault {
    log.debug2("auIds = {}, refetchDepth = {}, priority = {}, force = {}",
	auIds, refetchDepth, priority, force);

    List<RequestDeepCrawlResult> results =
	new ArrayList<RequestDeepCrawlResult>(auIds.size());

    // Loop through all the Archival Unit identifiers.
    for (String auId : auIds) {
      // Perform the request.
      results.add(requestDeepCrawlById(auId, refetchDepth, priority, force));
    }

    log.debug2("results = {}", results);
    return results;
  }

  /**
   * Requests the polling of an archival unit.
   * 
   * @param auId A String with the identifier (auid) of the archival unit.
   * @return a RequestPollResult with the result of the operation.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public RequestAuControlResult requestPollById(String auId)
      throws LockssWebServicesFault {
    log.debug2("auId = {}", auId);

    try {
      // TODO: REPLACE THIS BLOCK WITH THE ACTUAL IMPLEMENTATION.
      RequestAuControlResult result =
	  new RequestAuControlResult(auId, true, "It worked!");
      // TODO: END OF BLOCK TO BE REPLACED.

      log.debug2("result = {}", result);
      return result;
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }

  /**
   * Requests the polling of the archival units defined by a list with their
   * identifiers.
   * 
   * @param auIds A {@code List<String>} with the identifiers (auids) of the
   *              archival units.
   * @return a {@code List<RequestPollResult>} with the results of the
   * operation.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public List<RequestAuControlResult> requestPollByIdList(List<String> auIds)
      throws LockssWebServicesFault {
    log.debug2("auIds = {}", auIds);

    List<RequestAuControlResult> results =
	new ArrayList<RequestAuControlResult>(auIds.size());

    // Loop through all the Archival Unit identifiers.
    for (String auId : auIds) {
      // Perform the request.
      results.add(requestPollById(auId));
    }

    log.debug2("results = {}", results);
    return results;
  }

  /**
   * Requests the metadata indexing of an archival unit.
   * 
   * @param auId  A String with the identifier (auid) of the archival unit.
   * @param force A boolean with <code>true</code> if the request is to be made
   *              even in the presence of some anomalies, <code>false</code>
   *              otherwise.
   * @return a RequestAuControlResult with the result of the operation.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public RequestAuControlResult requestMdIndexingById(String auId,
      boolean force) throws LockssWebServicesFault {
    log.debug2("auId = {}, force = {}", auId, force);

    try {
      // TODO: REPLACE THIS BLOCK WITH THE ACTUAL IMPLEMENTATION.
      RequestAuControlResult result =
	  new RequestAuControlResult(auId, true, "It worked!");
      // TODO: END OF BLOCK TO BE REPLACED.

      log.debug2("result = {}", result);
      return result;
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }

  /**
   * Requests the metadata indexing of the archival units defined by a list with
   * their identifiers.
   * 
   * @param auIds A List<String> with the identifiers (auids) of the archival
   *              units.
   * @param force A boolean with <code>true</code> if the request is to be made
   *              even in the presence of some anomalies, <code>false</code>
   *              otherwise.
   * @return a List<RequestAuControlResult> with the results of the operation.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public List<RequestAuControlResult> requestMdIndexingByIdList(
      List<String> auIds, boolean force) throws LockssWebServicesFault {
    log.debug2("auIds = {}, force = {}", auIds, force);

    List<RequestAuControlResult> results =
	new ArrayList<RequestAuControlResult>(auIds.size());

    // Loop through all the Archival Unit identifiers.
    for (String auId : auIds) {
      // Perform the request.
      results.add(requestMdIndexingById(auId, force));
    }

    log.debug2("results = {}", results);
    return results;
  }

  /**
   * Disables the metadata indexing of an archival unit.
   * 
   * @param auId A String with the identifier (auid) of the archival unit.
   * @return a RequestAuControlResult with the result of the operation.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public RequestAuControlResult disableMdIndexingById(String auId)
      throws LockssWebServicesFault {
    log.debug2("auId = {}", auId);

    try {
      // TODO: REPLACE THIS BLOCK WITH THE ACTUAL IMPLEMENTATION.
      RequestAuControlResult result =
	  new RequestAuControlResult(auId, true, "It worked!");
      // TODO: END OF BLOCK TO BE REPLACED.

      log.debug2("result = {}", result);
      return result;
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }

  /**
   * Disables the metadata indexing of the archival units defined by a list with
   * their identifiers.
   * 
   * @param auIds A List<String> with the identifiers (auids) of the archival
   *              units.
   * @return a List<RequestAuControlResult> with the results of the operation.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public List<RequestAuControlResult> disableMdIndexingByIdList(
      List<String> auIds) throws LockssWebServicesFault {
    log.debug2("auIds = {}", auIds);

    List<RequestAuControlResult> results =
	new ArrayList<RequestAuControlResult>(auIds.size());

    // Loop through all the Archival Unit identifiers.
    for (String auId : auIds) {
      // Perform the request.
      results.add(disableMdIndexingById(auId));
    }

    log.debug2("results = {}", results);
    return results;
  }

  /**
   * Enables the metadata indexing of an archival unit.
   * 
   * @param auId A String with the identifier (auid) of the archival unit.
   * @return a RequestAuControlResult with the result of the operation.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public RequestAuControlResult enableMdIndexingById(String auId)
      throws LockssWebServicesFault {
    log.debug2("auId = {}", auId);

    try {
      // TODO: REPLACE THIS BLOCK WITH THE ACTUAL IMPLEMENTATION.
      RequestAuControlResult result =
	  new RequestAuControlResult(auId, true, "It worked!");
      // TODO: END OF BLOCK TO BE REPLACED.

      log.debug2("result = {}", result);
      return result;
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }

  /**
   * Enables the metadata indexing of the archival units defined by a list with
   * their identifiers.
   * 
   * @param auIds A List<String> with the identifiers (auids) of the archival
   *              units.
   * @return a List<RequestAuControlResult> with the results of the operation.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public List<RequestAuControlResult> enableMdIndexingByIdList(
      List<String> auIds) throws LockssWebServicesFault {
    log.debug2("auIds = {}", auIds);

    List<RequestAuControlResult> results =
	new ArrayList<RequestAuControlResult>(auIds.size());

    // Loop through all the Archival Unit identifiers.
    for (String auId : auIds) {
      // Perform the request.
      results.add(enableMdIndexingById(auId));
    }

    log.debug2("results = {}", results);
    return results;
  }
}
