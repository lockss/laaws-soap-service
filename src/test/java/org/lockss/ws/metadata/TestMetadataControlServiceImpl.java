package org.lockss.ws.metadata;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.lockss.log.L4JLogger;
import org.lockss.ws.entities.MetadataControlResult;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;

public class TestMetadataControlServiceImpl {
    @Mock
    L4JLogger log;
    @Mock
    Environment env;
    @InjectMocks
    MetadataControlServiceImpl metadataControlServiceImpl;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testDeletePublicationIssn() throws Exception {
        MetadataControlResult result = metadataControlServiceImpl.deletePublicationIssn(Long.valueOf(1), "issn", "issnType");
        Assert.assertEquals(null, result);
    }

    @Test
    public void testDeleteAu() throws Exception {
        MetadataControlResult result = metadataControlServiceImpl.deleteAu(Long.valueOf(1), "auKey");
        Assert.assertEquals(null, result);
    }
}
