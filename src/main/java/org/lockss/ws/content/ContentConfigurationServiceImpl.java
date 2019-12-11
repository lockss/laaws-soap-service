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
package org.lockss.ws.content;

import java.util.ArrayList;
import java.util.List;
import org.lockss.log.L4JLogger;
import org.lockss.ws.entities.ContentConfigurationResult;
import org.lockss.ws.entities.LockssWebServicesFault;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/**
 * The Content Configuration SOAP web service implementation.
 */
@Service
public class ContentConfigurationServiceImpl implements
    ContentConfigurationService {
  private final static L4JLogger log = L4JLogger.getLogger();

  @Autowired
  private Environment env;

  /**
   * Configures the archival unit defined by its identifier.
   * 
   * @param auId A String with the identifier (auid) of the archival unit. The
   *             archival unit to be added must already be in the title db
   *             that's loaded into the daemon.
   * @return a ContentConfigurationResult with the result of the operation.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public ContentConfigurationResult addAuById(String auId)
      throws LockssWebServicesFault {
    log.debug2("auId = {}", auId);

    List<String> auIds = new ArrayList<String>();
    auIds.add(auId);

    ContentConfigurationResult result = addAusByIdList(auIds).get(0);
    log.debug2("result = {}", result);
    return result;
  }

  /**
   * Configures the archival units defined by a list of their identifiers.
   * 
   * @param auIds A {@code List<String>} with the identifiers (auids) of the
   *              archival units. The archival units to be added must already be
   *              in the title db that's loaded into the daemon.
   * @return a {@code List<ContentConfigurationResult>} with the results of the
   *         operation.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public List<ContentConfigurationResult> addAusByIdList(List<String> auIds)
      throws LockssWebServicesFault {
    log.debug2("auIds = {}", auIds);

    try {
      // TODO: REPLACE THIS BLOCK WITH THE ACTUAL IMPLEMENTATION.
      List<ContentConfigurationResult> results =
	  new ArrayList<ContentConfigurationResult>(auIds.size());

      ContentConfigurationResult result =
	  new ContentConfigurationResult(auIds.get(0), "AU Name", Boolean.TRUE,
	      "It worked!");

      results.add(result);
      // TODO: END OF BLOCK TO BE REPLACED.

      log.debug2("results = {}", results);
      return results;
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }

  /**
   * Unconfigures the archival unit defined by its identifier.
   * 
   * @param auId A String with the identifier (auid) of the archival unit.
   * @return a ContentConfigurationResult with the result of the operation.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public ContentConfigurationResult deleteAuById(String auId)
      throws LockssWebServicesFault {
    log.debug2("auId = {}", auId);

    List<String> auIds = new ArrayList<String>(1);
    auIds.add(auId);

    // Delete the archival unit.
    ContentConfigurationResult result = deleteAusByIdList(auIds).get(0);

    log.debug2("result = {}", result);
    return result;
  }

  /**
   * Unconfigures the archival units defined by a list with their identifiers.
   * 
   * @param auIds A {@code List<String>} with the identifiers (auids) of the
   *              archival units.
   * @return a {@code List<ContentConfigurationResult>} with the results of the
   *         operation.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public List<ContentConfigurationResult> deleteAusByIdList(List<String> auIds)
      throws LockssWebServicesFault {
    log.debug2("auIds = {}", auIds);

    try {
      // TODO: REPLACE THIS BLOCK WITH THE ACTUAL IMPLEMENTATION.
      List<ContentConfigurationResult> results =
	  new ArrayList<ContentConfigurationResult>(auIds.size());

      ContentConfigurationResult result =
	  new ContentConfigurationResult(auIds.get(0), "AU Name", Boolean.TRUE,
	      "It worked!");

      results.add(result);
      // TODO: END OF BLOCK TO BE REPLACED.

      log.debug2("results = {}", results);
      return results;
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }

  /**
   * Reactivates the archival unit defined by its identifier.
   * 
   * @param auId A String with the identifier (auid) of the archival unit.
   * @return a ContentConfigurationResult with the result of the operation.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public ContentConfigurationResult reactivateAuById(String auId)
      throws LockssWebServicesFault {
    log.debug2("auId = {}", auId);

    List<String> auIds = new ArrayList<String>(1);
    auIds.add(auId);

    // Reactivate the archival unit.
    ContentConfigurationResult result = reactivateAusByIdList(auIds).get(0);

    log.debug2("result = {}", result);
    return result;
  }

  /**
   * reactivates the archival units defined by a list with their identifiers.
   * 
   * @param auIds A {@code List<String>} with the identifiers (auids) of the
   *              archival units.
   * @return a {@code List<ContentConfigurationResult>} with the results of the
   *         operation.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public List<ContentConfigurationResult> reactivateAusByIdList(
      List<String> auIds) throws LockssWebServicesFault {
    log.debug2("auIds = {}", auIds);

    try {
      // TODO: REPLACE THIS BLOCK WITH THE ACTUAL IMPLEMENTATION.
      List<ContentConfigurationResult> results =
	  new ArrayList<ContentConfigurationResult>(auIds.size());

      ContentConfigurationResult result =
	  new ContentConfigurationResult(auIds.get(0), "AU Name", Boolean.TRUE,
	      "It worked!");

      results.add(result);
      // TODO: END OF BLOCK TO BE REPLACED.

      log.debug2("results = {}", results);
      return results;
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }

  /**
   * Deactivates the archival unit defined by its identifier.
   * 
   * @param auId A String with the identifier (auid) of the archival unit.
   * @return a ContentConfigurationResult with the result of the operation.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public ContentConfigurationResult deactivateAuById(String auId)
      throws LockssWebServicesFault {
    log.debug2("auId = {}", auId);

    List<String> auIds = new ArrayList<String>(1);
    auIds.add(auId);

    // Deactivate the archival unit.
    ContentConfigurationResult result = deactivateAusByIdList(auIds).get(0);

    log.debug2("result = {}", result);
    return result;
  }

  /**
   * Deactivates the archival units defined by a list with their identifiers.
   * 
   * @param auIds A {@code List<String>} with the identifiers (auids) of the
   *              archival units.
   * @return a {@code List<ContentConfigurationResult>} with the results of the
   *         operation.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public List<ContentConfigurationResult> deactivateAusByIdList(
      List<String> auIds) throws LockssWebServicesFault {
    log.debug2("auIds = {}", auIds);

    try {
      // TODO: REPLACE THIS BLOCK WITH THE ACTUAL IMPLEMENTATION.
      List<ContentConfigurationResult> results =
	  new ArrayList<ContentConfigurationResult>(auIds.size());

      ContentConfigurationResult result =
	  new ContentConfigurationResult(auIds.get(0), "AU Name", Boolean.TRUE,
	      "It worked!");

      results.add(result);
      // TODO: END OF BLOCK TO BE REPLACED.

      log.debug2("results = {}", results);
      return results;
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }
}
