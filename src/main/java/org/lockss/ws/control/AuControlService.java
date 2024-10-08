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

import org.lockss.ws.entities.*;

import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;
import java.util.List;

/** The AU Control SOAP web service interface. */
@WebService
public interface AuControlService {
  /**
   * Provides an indication of whether an archival unit has substance.
   *
   * @param auId A String with the identifier (auid) of the archival unit.
   * @return a CheckSubstanceResult with the result of the operation.
   * @throws LockssWebServicesFault if there are problems.
   */
  @WebMethod
  CheckSubstanceResult checkSubstanceById(@WebParam(name = "auId") String auId)
      throws LockssWebServicesFault;

  /**
   * Provides an indication of whether the archival units defined by a list with their identifiers
   * have substance.
   *
   * @param auIds A {@code List<String>} with the identifiers (auids) of the archival units.
   * @return a {@code List<CheckSubstanceResult>} with the results of the operation.
   * @throws LockssWebServicesFault if there are problems.
   */
  @WebMethod
  List<CheckSubstanceResult> checkSubstanceByIdList(@WebParam(name = "auIds") List<String> auIds)
      throws LockssWebServicesFault;

  /**
   * Requests the crawl of an archival unit.
   *
   * @param auId A String with the identifier (auid) of the archival unit.
   * @param priority An Integer with the priority of the crawl request.
   * @param force A boolean with <code>true</code> if the request is to be made even in the presence
   *     of some anomalies, <code>false</code> otherwise.
   * @return a RequestCrawlResult with the result of the operation.
   * @throws LockssWebServicesFault if there are problems.
   */
  @WebMethod
  RequestCrawlResult requestCrawlById(
      @WebParam(name = "auId") String auId,
      @WebParam(name = "priority") Integer priority,
      @WebParam(name = "force") boolean force)
      throws LockssWebServicesFault;

  /**
   * Requests the crawl of the archival units defined by a list with their identifiers.
   *
   * @param auIds A {@code List<String>} with the identifiers (auids) of the archival units.
   * @param priority An Integer with the priority of the crawl request.
   * @param force A boolean with <code>true</code> if the request is to be made even in the presence
   *     of some anomalies, <code>false</code> otherwise.
   * @return a {@code List<RequestCrawlResult>} with the results of the operation.
   * @throws LockssWebServicesFault if there are problems.
   */
  @WebMethod
  List<RequestCrawlResult> requestCrawlByIdList(
      @WebParam(name = "auIds") List<String> auIds,
      @WebParam(name = "priority") Integer priority,
      @WebParam(name = "force") boolean force)
      throws LockssWebServicesFault;

  /**
   * Requests the deep crawl of an archival unit.
   *
   * @param auId A String with the identifier (auid) of the archival unit.
   * @param refetchDepth An int with the depth of the crawl request.
   * @param priority An Integer with the priority of the crawl request.
   * @param force A boolean with <code>true</code> if the request is to be made even in the presence
   *     of some anomalies, <code>false</code> otherwise.
   * @return a RequestDeepCrawlResult with the result of the operation.
   * @throws LockssWebServicesFault if there are problems.
   */
  @WebMethod
  RequestDeepCrawlResult requestDeepCrawlById(
      @WebParam(name = "auId") String auId,
      @WebParam(name = "refetchDepth") int refetchDepth,
      @WebParam(name = "priority") Integer priority,
      @WebParam(name = "force") boolean force)
      throws LockssWebServicesFault;

  /**
   * Requests the deep crawl of the archival units defined by a list with their identifiers.
   *
   * @param auIds A {@code List<String>} with the identifiers (auids) of the archival units.
   * @param refetchDepth An int with the depth of the crawl request.
   * @param priority An Integer with the priority of the crawl request.
   * @param force A boolean with <code>true</code> if the request is to be made even in the presence
   *     of some anomalies, <code>false</code> otherwise.
   * @return a {@code List<RequestDeepCrawlResult>} with the results of the operation.
   * @throws LockssWebServicesFault if there are problems.
   */
  @WebMethod
  List<RequestDeepCrawlResult> requestDeepCrawlByIdList(
      @WebParam(name = "auIds") List<String> auIds,
      @WebParam(name = "refetchDepth") int refetchDepth,
      @WebParam(name = "priority") Integer priority,
      @WebParam(name = "force") boolean force)
      throws LockssWebServicesFault;

  /**
   * Requests the polling of an archival unit.
   *
   * @param auId A String with the identifier (auid) of the archival unit.
   * @return a RequestAuControlResult with the result of the operation.
   * @throws LockssWebServicesFault if there are problems.
   */
  @WebMethod
  RequestAuControlResult requestPollById(@WebParam(name = "auId") String auId)
      throws LockssWebServicesFault;

  /**
   * Requests the polling of the archival units defined by a list with their identifiers.
   *
   * @param auIds A {@code List<String>} with the identifiers (auids) of the archival units.
   * @return a {@code List<RequestAuControlResult>} with the results of the operation.
   * @throws LockssWebServicesFault if there are problems.
   */
  @WebMethod
  List<RequestAuControlResult> requestPollByIdList(@WebParam(name = "auIds") List<String> auIds)
      throws LockssWebServicesFault;

  /**
   * Requests the metadata indexing of an archival unit.
   *
   * @param auId A String with the identifier (auid) of the archival unit.
   * @param force A boolean with <code>true</code> if the request is to be made even in the presence
   *     of some anomalies, <code>false</code> otherwise.
   * @return a RequestAuControlResult with the result of the operation.
   * @throws LockssWebServicesFault if there are problems.
   */
  @WebMethod
  RequestAuControlResult requestMdIndexingById(
      @WebParam(name = "auId") String auId, @WebParam(name = "force") boolean force)
      throws LockssWebServicesFault;

  /**
   * Requests the metadata indexing of the archival units defined by a list with their identifiers.
   *
   * @param auIds A List<String> with the identifiers (auids) of the archival units.
   * @param force A boolean with <code>true</code> if the request is to be made even in the presence
   *     of some anomalies, <code>false</code> otherwise.
   * @return a List<RequestAuControlResult> with the results of the operation.
   * @throws LockssWebServicesFault if there are problems.
   */
  @WebMethod
  List<RequestAuControlResult> requestMdIndexingByIdList(
      @WebParam(name = "auIds") List<String> auIds, @WebParam(name = "force") boolean force)
      throws LockssWebServicesFault;

  /**
   * Disables the metadata indexing of an archival unit.
   *
   * @param auId A String with the identifier (auid) of the archival unit.
   * @return a RequestAuControlResult with the result of the operation.
   * @throws LockssWebServicesFault if there are problems.
   */
  @WebMethod
  RequestAuControlResult disableMdIndexingById(@WebParam(name = "auId") String auId)
      throws LockssWebServicesFault;

  /**
   * Disables the metadata indexing of the archival units defined by a list with their identifiers.
   *
   * @param auIds A List<String> with the identifiers (auids) of the archival units.
   * @return a List<RequestAuControlResult> with the results of the operation.
   * @throws LockssWebServicesFault if there are problems.
   */
  @WebMethod
  List<RequestAuControlResult> disableMdIndexingByIdList(
      @WebParam(name = "auIds") List<String> auIds) throws LockssWebServicesFault;

  /**
   * Enables the metadata indexing of an archival unit.
   *
   * @param auId A String with the identifier (auid) of the archival unit.
   * @return a RequestAuControlResult with the result of the operation.
   * @throws LockssWebServicesFault if there are problems.
   */
  @WebMethod
  RequestAuControlResult enableMdIndexingById(@WebParam(name = "auId") String auId)
      throws LockssWebServicesFault;

  /**
   * Enables the metadata indexing of the archival units defined by a list with their identifiers.
   *
   * @param auIds A List<String> with the identifiers (auids) of the archival units.
   * @return a List<RequestAuControlResult> with the results of the operation.
   * @throws LockssWebServicesFault if there are problems.
   */
  @WebMethod
  List<RequestAuControlResult> enableMdIndexingByIdList(
      @WebParam(name = "auIds") List<String> auIds) throws LockssWebServicesFault;
}
