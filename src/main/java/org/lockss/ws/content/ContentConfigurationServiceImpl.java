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
package org.lockss.ws.content;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.lockss.log.L4JLogger;
import org.lockss.ws.BaseServiceImpl;
import org.lockss.ws.entities.ContentConfigurationResult;
import org.lockss.ws.entities.LockssWebServicesFault;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * The Content Configuration SOAP web service implementation.
 */
@Service
public class ContentConfigurationServiceImpl extends BaseServiceImpl
implements ContentConfigurationService {
  private final static L4JLogger log = L4JLogger.getLogger();

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
      // Make the REST call.
      ResponseEntity<String> response = callRestServiceEndpoint(
	  env.getProperty(CONFIG_SVC_URL_KEY), "/aus/add", null, null,
	  HttpMethod.POST, auIds, "Can't add AUs");

      // Get the response body.
      try {
        ObjectMapper mapper = new ObjectMapper();
        List<ContentConfigurationResult> result =
            mapper.readValue(response.getBody(),
        	new TypeReference<List<ContentConfigurationResult>>(){});

        log.debug2("result = " + result);
        return result;
      } catch (Exception e) {
        log.error("Cannot get body of response", e);
        throw e;
      }
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
      // Make the REST call.
      ResponseEntity<String> response = callRestServiceEndpoint(
	  env.getProperty(CONFIG_SVC_URL_KEY), "/aus/delete", null, null,
	  HttpMethod.DELETE, auIds, "Can't delete AUs");

      // Get the response body.
      try {
        ObjectMapper mapper = new ObjectMapper();
        List<ContentConfigurationResult> result =
            mapper.readValue(response.getBody(),
        	new TypeReference<List<ContentConfigurationResult>>(){});

        log.debug2("result = " + result);
        return result;
      } catch (Exception e) {
        log.error("Cannot get body of response", e);
        throw e;
      }
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
      // Make the REST call.
      ResponseEntity<String> response = callRestServiceEndpoint(
	  env.getProperty(CONFIG_SVC_URL_KEY), "/aus/reactivate", null, null,
	  HttpMethod.PUT, auIds, "Can't reactivate AUs");

      // Get the response body.
      try {
        ObjectMapper mapper = new ObjectMapper();
        List<ContentConfigurationResult> result =
            mapper.readValue(response.getBody(),
        	new TypeReference<List<ContentConfigurationResult>>(){});

        log.debug2("result = " + result);
        return result;
      } catch (Exception e) {
        log.error("Cannot get body of response", e);
        throw e;
      }
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
      // Make the REST call.
      ResponseEntity<String> response = callRestServiceEndpoint(
	  env.getProperty(CONFIG_SVC_URL_KEY), "/aus/deactivate", null, null,
	  HttpMethod.PUT, auIds, "Can't deactivate AUs");

      // Get the response body.
      try {
        ObjectMapper mapper = new ObjectMapper();
        List<ContentConfigurationResult> result =
            mapper.readValue(response.getBody(),
        	new TypeReference<List<ContentConfigurationResult>>(){});

        log.debug2("result = " + result);
        return result;
      } catch (Exception e) {
        log.error("Cannot get body of response", e);
        throw e;
      }
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }
}
