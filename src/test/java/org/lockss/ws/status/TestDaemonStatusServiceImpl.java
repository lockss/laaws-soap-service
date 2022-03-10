package org.lockss.ws.status;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.lockss.log.L4JLogger;
import org.lockss.ws.entities.AuStatus;
import org.lockss.ws.entities.AuWsResult;
import org.lockss.ws.entities.CrawlWsResult;
import org.lockss.ws.entities.IdNamePair;
import org.lockss.ws.entities.PeerWsResult;
import org.lockss.ws.entities.PlatformConfigurationWsResult;
import org.lockss.ws.entities.PluginWsResult;
import org.lockss.ws.entities.PollWsResult;
import org.lockss.ws.entities.RepositorySpaceWsResult;
import org.lockss.ws.entities.RepositoryWsResult;
import org.lockss.ws.entities.TdbAuWsResult;
import org.lockss.ws.entities.TdbPublisherWsResult;
import org.lockss.ws.entities.TdbTitleWsResult;
import org.lockss.ws.entities.VoteWsResult;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;

public class TestDaemonStatusServiceImpl {
    @Mock
    L4JLogger log;
    @Mock
    Environment env;
    @InjectMocks
    DaemonStatusServiceImpl daemonStatusServiceImpl;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testIsDaemonReady() throws Exception {
        boolean result = daemonStatusServiceImpl.isDaemonReady();
        Assert.assertEquals(true, result);
    }

    @Test
    public void testGetAuIds() throws Exception {
        Collection<IdNamePair> result = daemonStatusServiceImpl.getAuIds();
        Assert.assertEquals(Arrays.<IdNamePair>asList(null), result);
    }

    @Test
    public void testGetAuStatus() throws Exception {
        AuStatus result = daemonStatusServiceImpl.getAuStatus("auId");
        Assert.assertEquals(null, result);
    }

    @Test
    public void testQueryPlugins() throws Exception {
        List<PluginWsResult> result = daemonStatusServiceImpl.queryPlugins("pluginQuery");
        Assert.assertEquals(Arrays.<PluginWsResult>asList(null), result);
    }

    @Test
    public void testQueryAus() throws Exception {
        List<AuWsResult> result = daemonStatusServiceImpl.queryAus("auQuery");
        Assert.assertEquals(Arrays.<AuWsResult>asList(null), result);
    }

    @Test
    public void testQueryPeers() throws Exception {
        List<PeerWsResult> result = daemonStatusServiceImpl.queryPeers("peerQuery");
        Assert.assertEquals(Arrays.<PeerWsResult>asList(null), result);
    }

    @Test
    public void testQueryVotes() throws Exception {
        List<VoteWsResult> result = daemonStatusServiceImpl.queryVotes("voteQuery");
        Assert.assertEquals(Arrays.<VoteWsResult>asList(null), result);
    }

    @Test
    public void testQueryRepositorySpaces() throws Exception {
        List<RepositorySpaceWsResult> result = daemonStatusServiceImpl.queryRepositorySpaces("repositorySpaceQuery");
        Assert.assertEquals(Arrays.<RepositorySpaceWsResult>asList(null), result);
    }

    @Test
    public void testQueryRepositories() throws Exception {
        List<RepositoryWsResult> result = daemonStatusServiceImpl.queryRepositories("repositoryQuery");
        Assert.assertEquals(Arrays.<RepositoryWsResult>asList(null), result);
    }

    @Test
    public void testQueryCrawls() throws Exception {
        List<CrawlWsResult> result = daemonStatusServiceImpl.queryCrawls("crawlQuery");
        Assert.assertEquals(Arrays.<CrawlWsResult>asList(null), result);
    }

    @Test
    public void testQueryPolls() throws Exception {
        List<PollWsResult> result = daemonStatusServiceImpl.queryPolls("pollQuery");
        Assert.assertEquals(Arrays.<PollWsResult>asList(null), result);
    }

    @Test
    public void testGetPlatformConfiguration() throws Exception {
        PlatformConfigurationWsResult result = daemonStatusServiceImpl.getPlatformConfiguration();
        Assert.assertEquals(null, result);
    }

    @Test
    public void testQueryTdbPublishers() throws Exception {
        List<TdbPublisherWsResult> result = daemonStatusServiceImpl.queryTdbPublishers("tdbPublisherQuery");
        Assert.assertEquals(Arrays.<TdbPublisherWsResult>asList(null), result);
    }

    @Test
    public void testQueryTdbTitles() throws Exception {
        List<TdbTitleWsResult> result = daemonStatusServiceImpl.queryTdbTitles("tdbTitleQuery");
        Assert.assertEquals(Arrays.<TdbTitleWsResult>asList(null), result);
    }

    @Test
    public void testQueryTdbAus() throws Exception {
        List<TdbAuWsResult> result = daemonStatusServiceImpl.queryTdbAus("tdbAuQuery");
        Assert.assertEquals(Arrays.<TdbAuWsResult>asList(null), result);
    }

    @Test
    public void testGetAuUrls() throws Exception {
        List<String> result = daemonStatusServiceImpl.getAuUrls("auId", "url");
        Assert.assertEquals(Arrays.asList("String"), result);
    }
}
