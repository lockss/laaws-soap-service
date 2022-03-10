package org.lockss.ws.content;

import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.lockss.log.L4JLogger;
import org.lockss.ws.entities.FileWsResult;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;

public class TestContentServiceImpl {
    @Mock
    L4JLogger log;
    @Mock
    Environment env;
    @InjectMocks
    ContentServiceImpl contentServiceImpl;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetVersions() throws Exception {
        List<FileWsResult> result = contentServiceImpl.getVersions("url", "auId");
        Assert.assertEquals(Arrays.<FileWsResult>asList(null), result);
    }

    @Test
    public void testIsUrlCached() throws Exception {
        boolean result = contentServiceImpl.isUrlCached("url", "auId");
        Assert.assertEquals(true, result);
    }

    @Test
    public void testIsUrlVersionCached() throws Exception {
        boolean result = contentServiceImpl.isUrlVersionCached("url", "auId", Integer.valueOf(0));
        Assert.assertEquals(true, result);
    }
}
