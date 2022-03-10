package org.lockss.ws.metadata;

import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.lockss.log.L4JLogger;
import org.lockss.ws.entities.AuMetadataWsResult;
import org.lockss.ws.entities.KeyIdNamePairListPair;
import org.lockss.ws.entities.KeyValueListPair;
import org.lockss.ws.entities.MetadataItemWsResult;
import org.lockss.ws.entities.MismatchedMetadataChildWsResult;
import org.lockss.ws.entities.PkNamePairIdNamePairListPair;
import org.lockss.ws.entities.UnnamedItemWsResult;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;

public class TestMetadataMonitorServiceImpl {
    @Mock
    L4JLogger log;
    @Mock
    Environment env;
    @InjectMocks
    MetadataMonitorServiceImpl metadataMonitorServiceImpl;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetPublisherNames() throws Exception {
        List<String> result = metadataMonitorServiceImpl.getPublisherNames();
        Assert.assertEquals(Arrays.asList("String"), result);
    }

    @Test
    public void testGetPublishersWithMultipleDoiPrefixes() throws Exception {
        List<KeyValueListPair> result = metadataMonitorServiceImpl.getPublishersWithMultipleDoiPrefixes();
        Assert.assertEquals(Arrays.<KeyValueListPair>asList(null), result);
    }

    @Test
    public void testGetDoiPrefixesWithMultiplePublishers() throws Exception {
        List<KeyValueListPair> result = metadataMonitorServiceImpl.getDoiPrefixesWithMultiplePublishers();
        Assert.assertEquals(Arrays.<KeyValueListPair>asList(null), result);
    }

    @Test
    public void testGetAuIdsWithMultipleDoiPrefixes() throws Exception {
        List<KeyValueListPair> result = metadataMonitorServiceImpl.getAuIdsWithMultipleDoiPrefixes();
        Assert.assertEquals(Arrays.<KeyValueListPair>asList(null), result);
    }

    @Test
    public void testGetAuNamesWithMultipleDoiPrefixes() throws Exception {
        List<KeyValueListPair> result = metadataMonitorServiceImpl.getAuNamesWithMultipleDoiPrefixes();
        Assert.assertEquals(Arrays.<KeyValueListPair>asList(null), result);
    }

    @Test
    public void testGetPublicationsWithMoreThan2Isbns() throws Exception {
        List<KeyIdNamePairListPair> result = metadataMonitorServiceImpl.getPublicationsWithMoreThan2Isbns();
        Assert.assertEquals(Arrays.<KeyIdNamePairListPair>asList(null), result);
    }

    @Test
    public void testGetPublicationsWithMoreThan2Issns() throws Exception {
        List<KeyIdNamePairListPair> result = metadataMonitorServiceImpl.getPublicationsWithMoreThan2Issns();
        Assert.assertEquals(Arrays.<KeyIdNamePairListPair>asList(null), result);
    }

    @Test
    public void testGetIdPublicationsWithMoreThan2Issns() throws Exception {
        List<PkNamePairIdNamePairListPair> result = metadataMonitorServiceImpl.getIdPublicationsWithMoreThan2Issns();
        Assert.assertEquals(Arrays.<PkNamePairIdNamePairListPair>asList(null), result);
    }

    @Test
    public void testGetIsbnsWithMultiplePublications() throws Exception {
        List<KeyValueListPair> result = metadataMonitorServiceImpl.getIsbnsWithMultiplePublications();
        Assert.assertEquals(Arrays.<KeyValueListPair>asList(null), result);
    }

    @Test
    public void testGetIssnsWithMultiplePublications() throws Exception {
        List<KeyValueListPair> result = metadataMonitorServiceImpl.getIssnsWithMultiplePublications();
        Assert.assertEquals(Arrays.<KeyValueListPair>asList(null), result);
    }

    @Test
    public void testGetBooksWithIssns() throws Exception {
        List<KeyValueListPair> result = metadataMonitorServiceImpl.getBooksWithIssns();
        Assert.assertEquals(Arrays.<KeyValueListPair>asList(null), result);
    }

    @Test
    public void testGetPeriodicalsWithIsbns() throws Exception {
        List<KeyValueListPair> result = metadataMonitorServiceImpl.getPeriodicalsWithIsbns();
        Assert.assertEquals(Arrays.<KeyValueListPair>asList(null), result);
    }

    @Test
    public void testGetUnknownProviderAuIds() throws Exception {
        List<String> result = metadataMonitorServiceImpl.getUnknownProviderAuIds();
        Assert.assertEquals(Arrays.asList("String"), result);
    }

    @Test
    public void testGetMismatchedParentJournalArticles() throws Exception {
        List<MismatchedMetadataChildWsResult> result = metadataMonitorServiceImpl.getMismatchedParentJournalArticles();
        Assert.assertEquals(Arrays.<MismatchedMetadataChildWsResult>asList(null), result);
    }

    @Test
    public void testGetMismatchedParentBookChapters() throws Exception {
        List<MismatchedMetadataChildWsResult> result = metadataMonitorServiceImpl.getMismatchedParentBookChapters();
        Assert.assertEquals(Arrays.<MismatchedMetadataChildWsResult>asList(null), result);
    }

    @Test
    public void testGetMismatchedParentBookVolumes() throws Exception {
        List<MismatchedMetadataChildWsResult> result = metadataMonitorServiceImpl.getMismatchedParentBookVolumes();
        Assert.assertEquals(Arrays.<MismatchedMetadataChildWsResult>asList(null), result);
    }

    @Test
    public void testGetAuIdsWithMultiplePublishers() throws Exception {
        List<KeyValueListPair> result = metadataMonitorServiceImpl.getAuIdsWithMultiplePublishers();
        Assert.assertEquals(Arrays.<KeyValueListPair>asList(null), result);
    }

    @Test
    public void testGetAuNamesWithMultiplePublishers() throws Exception {
        List<KeyValueListPair> result = metadataMonitorServiceImpl.getAuNamesWithMultiplePublishers();
        Assert.assertEquals(Arrays.<KeyValueListPair>asList(null), result);
    }

    @Test
    public void testGetUnnamedItems() throws Exception {
        List<UnnamedItemWsResult> result = metadataMonitorServiceImpl.getUnnamedItems();
        Assert.assertEquals(Arrays.<UnnamedItemWsResult>asList(null), result);
    }

    @Test
    public void testGetPublicationsWithMultiplePids() throws Exception {
        List<KeyValueListPair> result = metadataMonitorServiceImpl.getPublicationsWithMultiplePids();
        Assert.assertEquals(Arrays.<KeyValueListPair>asList(null), result);
    }

    @Test
    public void testGetNoDoiItems() throws Exception {
        List<MetadataItemWsResult> result = metadataMonitorServiceImpl.getNoDoiItems();
        Assert.assertEquals(Arrays.<MetadataItemWsResult>asList(null), result);
    }

    @Test
    public void testGetNoAccessUrlItems() throws Exception {
        List<MetadataItemWsResult> result = metadataMonitorServiceImpl.getNoAccessUrlItems();
        Assert.assertEquals(Arrays.<MetadataItemWsResult>asList(null), result);
    }

    @Test
    public void testGetNoItemsAuIds() throws Exception {
        List<String> result = metadataMonitorServiceImpl.getNoItemsAuIds();
        Assert.assertEquals(Arrays.asList("String"), result);
    }

    @Test
    public void testGetAuMetadata() throws Exception {
        AuMetadataWsResult result = metadataMonitorServiceImpl.getAuMetadata("auId");
        Assert.assertEquals(null, result);
    }

    @Test
    public void testGetDbArchivalUnitsDeletedFromDaemon() throws Exception {
        List<AuMetadataWsResult> result = metadataMonitorServiceImpl.getDbArchivalUnitsDeletedFromDaemon();
        Assert.assertEquals(Arrays.<AuMetadataWsResult>asList(null), result);
    }
}
