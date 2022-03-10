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
package org.lockss.ws.metadata;

import java.util.List;
import javax.jws.WebMethod;
import javax.jws.WebService;
import org.lockss.ws.entities.AuMetadataWsResult;
import org.lockss.ws.entities.KeyIdNamePairListPair;
import org.lockss.ws.entities.KeyValueListPair;
import org.lockss.ws.entities.LockssWebServicesFault;
import org.lockss.ws.entities.MetadataItemWsResult;
import org.lockss.ws.entities.MismatchedMetadataChildWsResult;
import org.lockss.ws.entities.PkNamePairIdNamePairListPair;
import org.lockss.ws.entities.UnnamedItemWsResult;

/** The Metadata Monitor SOAP web service interface. */
@WebService
public interface MetadataMonitorService {
  /**
   * Provides the names of the publishers in the database.
   *
   * @return a {@code List<String>} with the publisher names.
   * @throws LockssWebServicesFault if there are problems.
   */
  @WebMethod
  List<String> getPublisherNames() throws LockssWebServicesFault;

  /**
   * Provides the DOI prefixes for the publishers in the database with multiple DOI prefixes.
   *
   * @return a {@code List<KeyValueListPair>} with the DOI prefixes keyed by the publisher name.
   * @throws LockssWebServicesFault if there are problems.
   */
  @WebMethod
  List<KeyValueListPair> getPublishersWithMultipleDoiPrefixes() throws LockssWebServicesFault;

  /**
   * Provides the publisher names linked to DOI prefixes in the database that are linked to multiple
   * publishers.
   *
   * @return a {@code List<KeyValueListPair>} with the publisher names keyed by the DOI prefixes to
   *     which they are linked.
   * @throws LockssWebServicesFault if there are problems.
   */
  @WebMethod
  List<KeyValueListPair> getDoiPrefixesWithMultiplePublishers() throws LockssWebServicesFault;

  /**
   * Provides the DOI prefixes for the Archival Units in the database with multiple DOI prefixes.
   *
   * @return a {@code List<KeyValueListPair>} with the DOI prefixes keyed by the Archival Unit
   *     identifier.
   * @throws LockssWebServicesFault if there are problems.
   */
  @WebMethod
  List<KeyValueListPair> getAuIdsWithMultipleDoiPrefixes() throws LockssWebServicesFault;

  /**
   * Provides the DOI prefixes for the Archival Units in the database with multiple DOI prefixes.
   *
   * @return a {@code List<KeyValueListPair>} with the DOI prefixes keyed by the Archival Unit name.
   * @throws LockssWebServicesFault if there are problems.
   */
  @WebMethod
  List<KeyValueListPair> getAuNamesWithMultipleDoiPrefixes() throws LockssWebServicesFault;

  /**
   * Provides the ISBNs for the publications in the database with more than two ISBNS.
   *
   * @return a {@code List<KeyIdNamePairListPair>} with the ISBNs keyed by the publication name. The
   *     IdNamePair objects contain the ISBN as the identifier and the ISBN type as the name.
   * @throws LockssWebServicesFault if there are problems.
   */
  @WebMethod
  List<KeyIdNamePairListPair> getPublicationsWithMoreThan2Isbns() throws LockssWebServicesFault;

  /**
   * Provides the ISSNs for the publications in the database with more than two ISSNS.
   *
   * @return a {@code List<KeyIdNamePairListPair>} with the ISSNs keyed by the publication name. The
   *     IdNamePair objects contain the ISSN as the identifier and the ISSN type as the name.
   * @throws LockssWebServicesFault if there are problems.
   */
  @WebMethod
  List<KeyIdNamePairListPair> getPublicationsWithMoreThan2Issns() throws LockssWebServicesFault;

  /**
   * Provides the ISSNs for the publications in the database with more than two ISSNS.
   *
   * @return a {@code List<PkNamePairIdNamePairListPair>} with the ISSNs keyed by the publication
   *     PK/name pair. The IdNamePair objects contain the ISSN as the identifier and the ISSN type
   *     as the name.
   * @throws LockssWebServicesFault if there are problems.
   */
  @WebMethod
  List<PkNamePairIdNamePairListPair> getIdPublicationsWithMoreThan2Issns()
      throws LockssWebServicesFault;

  /**
   * Provides the publication names linked to ISBNs in the database that are linked to multiple
   * publications.
   *
   * @return a {@code List<KeyValueListPair>} with the publication names keyed by the ISBNs to which
   *     they are linked.
   * @throws LockssWebServicesFault if there are problems.
   */
  @WebMethod
  List<KeyValueListPair> getIsbnsWithMultiplePublications() throws LockssWebServicesFault;

  /**
   * Provides the publication names linked to ISSNs in the database that are linked to multiple
   * publications.
   *
   * @return a {@code List<KeyValueListPair>} with the publication names keyed by the ISSNs to which
   *     they are linked.
   * @throws LockssWebServicesFault if there are problems.
   */
  @WebMethod
  List<KeyValueListPair> getIssnsWithMultiplePublications() throws LockssWebServicesFault;

  /**
   * Provides the ISSNs for books in the database.
   *
   * @return a {@code List<KeyValueListPair>} with the ISSNs keyed by the publication name.
   * @throws LockssWebServicesFault if there are problems.
   */
  @WebMethod
  List<KeyValueListPair> getBooksWithIssns() throws LockssWebServicesFault;

  /**
   * Provides the ISBNs for periodicals in the database.
   *
   * @return a {@code List<KeyValueListPair>} with the ISBNs keyed by the publication name.
   * @throws LockssWebServicesFault if there are problems.
   */
  @WebMethod
  List<KeyValueListPair> getPeriodicalsWithIsbns() throws LockssWebServicesFault;

  /**
   * Provides the Archival Units in the database with an unknown provider.
   *
   * @return a {@code List<String>} with the sorted Archival Unit identifiers.
   * @throws LockssWebServicesFault if there are problems.
   */
  @WebMethod
  List<String> getUnknownProviderAuIds() throws LockssWebServicesFault;

  /**
   * Provides the journal articles in the database whose parent is not a journal.
   *
   * @return a {@code List<MismatchedChildWsResult>} with the mismatched journal articles sorted by
   *     Archival Unit, parent name and child name.
   * @throws LockssWebServicesFault if there are problems.
   */
  @WebMethod
  List<MismatchedMetadataChildWsResult> getMismatchedParentJournalArticles()
      throws LockssWebServicesFault;

  /**
   * Provides the book chapters in the database whose parent is not a book or a book series.
   *
   * @return a {@code List<MismatchedChildWsResult>} with the mismatched book chapters sorted by
   *     Archival Unit, parent name and child name.
   * @throws LockssWebServicesFault if there are problems.
   */
  @WebMethod
  List<MismatchedMetadataChildWsResult> getMismatchedParentBookChapters()
      throws LockssWebServicesFault;

  /**
   * Provides the book volumes in the database whose parent is not a book or a book series.
   *
   * @return a {@code List<MismatchedChildWsResult>} with the mismatched book volumes sorted by
   *     Archival Unit, parent name and child name.
   * @throws LockssWebServicesFault if there are problems.
   */
  @WebMethod
  List<MismatchedMetadataChildWsResult> getMismatchedParentBookVolumes()
      throws LockssWebServicesFault;

  /**
   * Provides the publishers for the Archival Units in the database with multiple publishers.
   *
   * @return a {@code List<KeyValueListPair>} with the publishers keyed by the Archival Unit
   *     identifier.
   * @throws LockssWebServicesFault if there are problems.
   */
  @WebMethod
  List<KeyValueListPair> getAuIdsWithMultiplePublishers() throws LockssWebServicesFault;

  /**
   * Provides the publishers for the Archival Units in the database with multiple publishers.
   *
   * @return a {@code List<KeyValueListPair>} with the publishers keyed by the Archival Unit name.
   * @throws LockssWebServicesFault if there are problems.
   */
  @WebMethod
  List<KeyValueListPair> getAuNamesWithMultiplePublishers() throws LockssWebServicesFault;

  /**
   * Provides the metadata items in the database that do not have a name.
   *
   * @return a {@code List<UnnamedItemWsResult>} with the unnamed metadata items sorted by
   *     publisher, Archival Unit, parent type, parent name and item type.
   * @throws LockssWebServicesFault if there are problems.
   */
  @WebMethod
  List<UnnamedItemWsResult> getUnnamedItems() throws LockssWebServicesFault;

  /**
   * Provides the proprietary identifiers for the publications in the database with multiple
   * proprietary identifiers.
   *
   * @return a {@code List<KeyValueListPair>} with the proprietary identifiers keyed by the
   *     publication name.
   * @throws LockssWebServicesFault if there are problems.
   */
  @WebMethod
  List<KeyValueListPair> getPublicationsWithMultiplePids() throws LockssWebServicesFault;

  /**
   * Provides the non-parent metadata items in the database that have no DOI.
   *
   * @return a {@code List<MetadataItemWsResult>} with the non-parent metadata items that have no
   *     DOI sorted by publisher, Archival Unit, parent type, parent name, item type and item name.
   * @throws LockssWebServicesFault if there are problems.
   */
  @WebMethod
  List<MetadataItemWsResult> getNoDoiItems() throws LockssWebServicesFault;

  /**
   * Provides the non-parent metadata items in the database that have no Access URL.
   *
   * @return a {@code List<MetadataItemWsResult>} with the non-parent metadata items that have no
   *     Access URL sorted by publisher, Archival Unit, parent type, parent name, item type and item
   *     name.
   * @throws LockssWebServicesFault if there are problems.
   */
  @WebMethod
  List<MetadataItemWsResult> getNoAccessUrlItems() throws LockssWebServicesFault;

  /**
   * Provides the Archival Units in the database with no metadata items.
   *
   * @return a {@code List<String>} with the sorted Archival Unit identifiers.
   * @throws LockssWebServicesFault if there are problems.
   */
  @WebMethod
  List<String> getNoItemsAuIds() throws LockssWebServicesFault;

  /**
   * Provides the metadata information of an archival unit in the system.
   *
   * @param auId A String with the identifier of the archival unit.
   * @return an AuMetadataWsResult with the metadata information of the archival unit.
   * @throws LockssWebServicesFault if there are problems.
   */
  @WebMethod
  AuMetadataWsResult getAuMetadata(String auId) throws LockssWebServicesFault;

  /**
   * Provides the Archival Units that exist in the database but that have been deleted from the
   * daemon.
   *
   * @return a {@code List<AuMetadataWsResult>} with the Archival unit data.
   * @throws LockssWebServicesFault if there are problems.
   */
  @WebMethod
  List<AuMetadataWsResult> getDbArchivalUnitsDeletedFromDaemon() throws LockssWebServicesFault;
}
