package org.lockss.ws.content;

import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.lockss.log.L4JLogger;
import org.lockss.util.test.LockssTestCase5;
import org.lockss.ws.entities.ContentConfigurationResult;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;

public class TestContentConfigurationServiceImpl extends LockssTestCase5 {
    private static L4JLogger log = L4JLogger.getLogger();
    @Mock
    Environment env;
    @InjectMocks
    ContentConfigurationServiceImpl contentConfigurationServiceImpl;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testAddAuById() throws Exception {
        ContentConfigurationResult expected = new ContentConfigurationResult();

        ContentConfigurationResult result = contentConfigurationServiceImpl.addAuById("auId");
        Assert.assertEquals(null, result);
    }

    @Test
    public void testAddAusByIdList() throws Exception {
        List<ContentConfigurationResult> result = contentConfigurationServiceImpl.addAusByIdList(
            Arrays.asList("String"));
        Assert.assertEquals(Arrays.<ContentConfigurationResult>asList(null), result);
    }

    @Test
    public void testDeleteAuById() throws Exception {
        ContentConfigurationResult result = contentConfigurationServiceImpl.deleteAuById("auId");
        Assert.assertEquals(null, result);
    }

    @Test
    public void testDeleteAusByIdList() throws Exception {
        List<ContentConfigurationResult> result = contentConfigurationServiceImpl.deleteAusByIdList(
            Arrays.asList("String"));
        Assert.assertEquals(Arrays.<ContentConfigurationResult>asList(null), result);
    }

    @Test
    public void testReactivateAuById() throws Exception {
        ContentConfigurationResult result = contentConfigurationServiceImpl.reactivateAuById("auId");
        Assert.assertEquals(null, result);
    }

    @Test
    public void testReactivateAusByIdList() throws Exception {
        List<ContentConfigurationResult> result = contentConfigurationServiceImpl.reactivateAusByIdList(
            Arrays.asList("String"));
        Assert.assertEquals(Arrays.<ContentConfigurationResult>asList(null), result);
    }

    @Test
    public void testDeactivateAuById() throws Exception {
        ContentConfigurationResult result = contentConfigurationServiceImpl.deactivateAuById("auId");
        Assert.assertEquals(null, result);
    }

    @Test
    public void testDeactivateAusByIdList() throws Exception {
        List<ContentConfigurationResult> result = contentConfigurationServiceImpl.deactivateAusByIdList(
            Arrays.asList("String"));
        Assert.assertEquals(Arrays.<ContentConfigurationResult>asList(null), result);
    }
}
