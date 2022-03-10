package org.lockss.ws.reports;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.lockss.log.L4JLogger;
import org.lockss.ws.entities.CounterReportResult;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;

public class TestCounterReportsServiceImpl {
    @Mock
    L4JLogger log;
    @Mock
    Environment env;
    @InjectMocks
    CounterReportsServiceImpl counterReportsServiceImpl;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetCounterReport() throws Exception {
        CounterReportResult result = counterReportsServiceImpl.getCounterReport(null);
        Assert.assertEquals(null, result);
    }
}
