package org.lockss.ws.importer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.lockss.log.L4JLogger;
import org.lockss.ws.entities.ImportWsResult;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;

public class TestImportServiceImpl {
    @Mock
    L4JLogger log;
    @Mock
    Environment env;
    @InjectMocks
    ImportServiceImpl importServiceImpl;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testImportPulledFile() throws Exception {
        ImportWsResult result = importServiceImpl.importPulledFile(null);
        Assert.assertEquals(null, result);
    }

    @Test
    public void testImportPushedFile() throws Exception {
        ImportWsResult result = importServiceImpl.importPushedFile(null);
        Assert.assertEquals(null, result);
    }

    @Test
    public void testGetSupportedChecksumAlgorithms() throws Exception {
        String[] result = importServiceImpl.getSupportedChecksumAlgorithms();
        Assert.assertArrayEquals(new String[]{"replaceMeWithExpectedResult"}, result);
    }
}
