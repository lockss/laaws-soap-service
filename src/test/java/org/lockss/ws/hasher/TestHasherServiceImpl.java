package org.lockss.ws.hasher;

import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.lockss.log.L4JLogger;
import org.lockss.ws.entities.HasherWsAsynchronousResult;
import org.lockss.ws.entities.HasherWsResult;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;

public class TestHasherServiceImpl {
    @Mock
    L4JLogger log;
    @Mock
    Environment env;
    @InjectMocks
    HasherServiceImpl hasherServiceImpl;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testHash() throws Exception {
        HasherWsResult result = hasherServiceImpl.hash(null);
        Assert.assertEquals(null, result);
    }

    @Test
    public void testHashAsynchronously() throws Exception {
        HasherWsAsynchronousResult result = hasherServiceImpl.hashAsynchronously(null);
        Assert.assertEquals(null, result);
    }

    @Test
    public void testGetAsynchronousHashResult() throws Exception {
        HasherWsAsynchronousResult result = hasherServiceImpl.getAsynchronousHashResult("requestId");
        Assert.assertEquals(null, result);
    }

    @Test
    public void testGetAllAsynchronousHashResults() throws Exception {
        List<HasherWsAsynchronousResult> result = hasherServiceImpl.getAllAsynchronousHashResults();
        Assert.assertEquals(Arrays.<HasherWsAsynchronousResult>asList(null), result);
    }

    @Test
    public void testRemoveAsynchronousHashRequest() throws Exception {
        HasherWsAsynchronousResult result = hasherServiceImpl.removeAsynchronousHashRequest("requestId");
        Assert.assertEquals(null, result);
    }
}
