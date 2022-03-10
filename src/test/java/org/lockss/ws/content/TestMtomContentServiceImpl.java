package org.lockss.ws.content;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.lockss.log.L4JLogger;
import org.lockss.ws.entities.ContentResult;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;

public class TestMtomContentServiceImpl {
    @Mock
    L4JLogger log;
    @Mock
    Environment env;
    @InjectMocks
    MtomContentServiceImpl mtomContentServiceImpl;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testFetchFile() throws Exception {
        ContentResult result = mtomContentServiceImpl.fetchFile("url", "auId");
        Assert.assertEquals(null, result);
    }

    @Test
    public void testFetchVersionedFile() throws Exception {
        ContentResult result = mtomContentServiceImpl.fetchVersionedFile("url", "auId", Integer.valueOf(0));
        Assert.assertEquals(null, result);
    }
}
