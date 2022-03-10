package org.lockss.ws.control;

import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.lockss.log.L4JLogger;
import org.lockss.ws.entities.CheckSubstanceResult;
import org.lockss.ws.entities.RequestAuControlResult;
import org.lockss.ws.entities.RequestCrawlResult;
import org.lockss.ws.entities.RequestDeepCrawlResult;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;

public class TestAuControlServiceImpl {
    @Mock
    L4JLogger log;
    @Mock
    Environment env;
    @InjectMocks
    AuControlServiceImpl auControlServiceImpl;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCheckSubstanceById() throws Exception {
        CheckSubstanceResult result = auControlServiceImpl.checkSubstanceById("auId");
        Assert.assertEquals(null, result);
    }

    @Test
    public void testCheckSubstanceByIdList() throws Exception {
        List<CheckSubstanceResult> result = auControlServiceImpl.checkSubstanceByIdList(
            Arrays.asList("String"));
        Assert.assertEquals(Arrays.<CheckSubstanceResult>asList(null), result);
    }

    @Test
    public void testRequestCrawlById() throws Exception {
        RequestCrawlResult result = auControlServiceImpl.requestCrawlById("auId", Integer.valueOf(0), true);
        Assert.assertEquals(null, result);
    }

    @Test
    public void testRequestCrawlByIdList() throws Exception {
        List<RequestCrawlResult> result = auControlServiceImpl.requestCrawlByIdList(Arrays.asList("String"), Integer.valueOf(0), true);
        Assert.assertEquals(Arrays.<RequestCrawlResult>asList(null), result);
    }

    @Test
    public void testRequestDeepCrawlById() throws Exception {
        RequestDeepCrawlResult result = auControlServiceImpl.requestDeepCrawlById("auId", 0, Integer.valueOf(0), true);
        Assert.assertEquals(null, result);
    }

    @Test
    public void testRequestDeepCrawlByIdList() throws Exception {
        List<RequestDeepCrawlResult> result = auControlServiceImpl.requestDeepCrawlByIdList(
            Arrays.asList("String"), 0, Integer.valueOf(0), true);
        Assert.assertEquals(Arrays.<RequestDeepCrawlResult>asList(null), result);
    }

    @Test
    public void testRequestPollById() throws Exception {
        RequestAuControlResult result = auControlServiceImpl.requestPollById("auId");
        Assert.assertEquals(null, result);
    }

    @Test
    public void testRequestPollByIdList() throws Exception {
        List<RequestAuControlResult> result = auControlServiceImpl.requestPollByIdList(
            Arrays.asList("String"));
        Assert.assertEquals(Arrays.<RequestAuControlResult>asList(null), result);
    }

    @Test
    public void testRequestMdIndexingById() throws Exception {
        RequestAuControlResult result = auControlServiceImpl.requestMdIndexingById("auId", true);
        Assert.assertEquals(null, result);
    }

    @Test
    public void testRequestMdIndexingByIdList() throws Exception {
        List<RequestAuControlResult> result = auControlServiceImpl.requestMdIndexingByIdList(
            Arrays.asList("String"), true);
        Assert.assertEquals(Arrays.<RequestAuControlResult>asList(null), result);
    }

    @Test
    public void testDisableMdIndexingById() throws Exception {
        RequestAuControlResult result = auControlServiceImpl.disableMdIndexingById("auId");
        Assert.assertEquals(null, result);
    }

    @Test
    public void testDisableMdIndexingByIdList() throws Exception {
        List<RequestAuControlResult> result = auControlServiceImpl.disableMdIndexingByIdList(
            Arrays.asList("String"));
        Assert.assertEquals(Arrays.<RequestAuControlResult>asList(null), result);
    }

    @Test
    public void testEnableMdIndexingById() throws Exception {
        RequestAuControlResult result = auControlServiceImpl.enableMdIndexingById("auId");
        Assert.assertEquals(null, result);
    }

    @Test
    public void testEnableMdIndexingByIdList() throws Exception {
        List<RequestAuControlResult> result = auControlServiceImpl.enableMdIndexingByIdList(
            Arrays.asList("String"));
        Assert.assertEquals(Arrays.<RequestAuControlResult>asList(null), result);
    }
}
