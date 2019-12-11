/*

 Copyright (c) 2013-2019 Board of Trustees of Leland Stanford Jr. University,
 all rights reserved.

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL
 STANFORD UNIVERSITY BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
 IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 Except as contained in this notice, the name of Stanford University shall not
 be used in advertising or otherwise to promote the sale, use or other dealings
 in this Software without prior written authorization from Stanford University.

 */
package org.lockss.ws.status;

import static org.lockss.ws.SoapServiceUtil.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.lockss.log.L4JLogger;
import org.lockss.util.rest.RestUtil;
import org.lockss.util.rest.exception.LockssRestException;
import org.lockss.util.rest.status.RestStatusClient;
import org.lockss.ws.SoapServiceUtil;
import org.lockss.ws.entities.AuStatus;
import org.lockss.ws.entities.AuWsResult;
import org.lockss.ws.entities.CrawlWsResult;
import org.lockss.ws.entities.IdNamePair;
import org.lockss.ws.entities.LockssWebServicesFault;
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
import org.lockss.ws.status.DaemonStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * The Daemon Status SOAP web service implementation.
 */
@Service
public class DaemonStatusServiceImpl implements DaemonStatusService {
  private final static L4JLogger log = L4JLogger.getLogger();

  @Autowired
  private Environment env;

  /**
   * Provides an indication of whether the daemon is ready.
   * 
   * @return a boolean with the indication.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public boolean isDaemonReady() throws LockssWebServicesFault {
    log.debug2("Invoked.");

    try {
      boolean result = true;

      // Check the repository service.
      String url = env.getProperty(REPO_SVC_URL_KEY);

      if (url != null) {
	result = isServiceReady(url);

	if (!result) {
	  log.debug2("result = {}", result);
	  return result;
	}
      }

      // Check the configuration service.
      url = env.getProperty(CONFIG_SVC_URL_KEY);

      if (url != null) {
	result = isServiceReady(url);

	if (!result) {
	  log.debug2("result = {}", result);
	  return result;
	}
      }

      // Check the poller service.
      url = env.getProperty(POLLER_SVC_URL_KEY);

      if (url != null) {
	result = isServiceReady(url);

	if (!result) {
	  log.debug2("result = {}", result);
	  return result;
	}
      }

      // Check the metadata extractor service.
      url = env.getProperty(MDX_SVC_URL_KEY);

      if (url != null) {
	result = isServiceReady(url);

	if (!result) {
	  log.debug2("result = {}", result);
	  return result;
	}
      }

      // Check the metadata service.
      url = env.getProperty(MDQ_SVC_URL_KEY);

      if (url != null) {
	result = isServiceReady(url);
      }

      log.debug2("result = {}", result);
      return result;
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }

  /**
   * Provides an indication of whether a REST service is ready.
   * 
   * @param serviceUrl A String with the URL of the service.
   * @return a boolean with the indication.
   */
  private boolean isServiceReady(String serviceUrl) {
    log.debug2("serviceUrl = {}", serviceUrl);
    boolean isReady = false;

    try {
      isReady = new RestStatusClient(serviceUrl).getStatus().isReady();
    } catch (LockssRestException lre) {
      log.debug("Ignored exception caught getting status of " + serviceUrl,
	  lre);
    }

    log.debug2("isReady = {}", isReady);
    return isReady;
  }

  /**
   * Provides a list of the identifier/name pairs of the archival units in the
   * system.
   * 
   * @return a {@code List<IdNamePair>} with the identifier/name pairs of the
   *         archival units in the system.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public Collection<IdNamePair> getAuIds() throws LockssWebServicesFault {
    log.debug2("Invoked.");

    try {
      RestTemplate restTemplate = SoapServiceUtil.getRestTemplate();

      // TODO: Specify the appropriate endpoint.
      String endpoint = env.getProperty(REPO_SVC_URL_KEY) + "/???";
      log.trace("endpoint = {}", endpoint);

      UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(endpoint);

      HttpHeaders restHeaders =
	  SoapServiceUtil.addSoapCredentials(new HttpHeaders());

      // Make the request to the REST service and get the response.
      ResponseEntity<String> response = RestUtil.callRestService(restTemplate,
	  builder.build().encode().toUri(), HttpMethod.GET,
	  new HttpEntity<>(null, restHeaders), String.class, "getAuIds");

      // Validate the response.
      if (!response.getStatusCode().is2xxSuccessful()) {
	throw new RuntimeException("RestUtil returned non-200 result");
      }

      // Extract the results.
      List<IdNamePair> results = new ObjectMapper().readValue(
	  (String)response.getBody(), new TypeReference<List<IdNamePair>>(){});

      log.debug2("results = {}", results);
      return results;
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }

  /**
   * Provides the status information of an archival unit in the system.
   * 
   * @param auId A String with the identifier of the archival unit.
   * @return an AuStatus with the status information of the archival unit.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public AuStatus getAuStatus(String auId) throws LockssWebServicesFault {
    log.debug2("auId = {}", auId);

    try {
      // TODO: REPLACE THIS BLOCK WITH THE ACTUAL IMPLEMENTATION.
      AuStatus result = new AuStatus();
      // TODO: END OF BLOCK TO BE REPLACED.

      log.debug2("result = {}", result);
      return result;
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }

  /**
   * Provides the selected properties of selected plugins in the system.
   * 
   * @param pluginQuery A String with the
   *                    <a href="package-summary.html#SQL-Like_Query">SQL-like
   *                    query</a> used to specify what properties to retrieve
   *                    from which plugins.
   * @return a {@code List<PluginWsResult>} with the results.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public List<PluginWsResult> queryPlugins(String pluginQuery)
      throws LockssWebServicesFault {
    log.debug2("pluginQuery = {}", pluginQuery);

    try {
      // TODO: REPLACE THIS BLOCK WITH THE ACTUAL IMPLEMENTATION.
      List<PluginWsResult> results = new ArrayList<>();
      // TODO: END OF BLOCK TO BE REPLACED.

      log.debug2("results = {}", results);
      return results;
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }

  /**
   * Provides the selected properties of selected archival units in the system.
   * 
   * @param auQuery A String with the
   *                <a href="package-summary.html#SQL-Like_Query">SQL-like
   *                query</a> used to specify what properties to retrieve from
   *                which archival units.
   * @return a {@code List<AuWsResult>} with the results.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public List<AuWsResult> queryAus(String auQuery) throws LockssWebServicesFault
  {
    log.debug2("auQuery = {}", auQuery);

    try {
      // TODO: REPLACE THIS BLOCK WITH THE ACTUAL IMPLEMENTATION.
      List<AuWsResult> results = new ArrayList<>();
      // TODO: END OF BLOCK TO BE REPLACED.

      log.debug2("results = {}", results);
      return results;
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }

  /**
   * Provides the selected properties of selected peers in the system.
   * 
   * @param peerQuery A String with the
   *                  <a href="package-summary.html#SQL-Like_Query">SQL-like
   *                  query</a> used to specify what properties to retrieve from
   *                  which peers.
   * @return a {@code List<PeerWsResult>} with the results.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public List<PeerWsResult> queryPeers(String peerQuery)
      throws LockssWebServicesFault {
    log.debug2("peerQuery = {}", peerQuery);

    try {
      // TODO: REPLACE THIS BLOCK WITH THE ACTUAL IMPLEMENTATION.
      List<PeerWsResult> results = new ArrayList<>();
      // TODO: END OF BLOCK TO BE REPLACED.

      log.debug2("results = {}", results);
      return results;
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }

  /**
   * Provides the selected properties of selected votes in the system.
   * 
   * @param voteQuery A String with the
   *                  <a href="package-summary.html#SQL-Like_Query">SQL-like
   *                  query</a> used to specify what properties to retrieve from
   *                  which votes.
   * @return a {@code List<VoteWsResult>} with the results.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public List<VoteWsResult> queryVotes(String voteQuery)
      throws LockssWebServicesFault {
    log.debug2("voteQuery = {}", voteQuery);

    try {
      // TODO: REPLACE THIS BLOCK WITH THE ACTUAL IMPLEMENTATION.
      List<VoteWsResult> results = new ArrayList<>();
      // TODO: END OF BLOCK TO BE REPLACED.

      log.debug2("results = {}", results);
      return results;
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }

  /**
   * Provides the selected properties of selected repository spaces in the
   * system.
   * @param repositorySpaceQuery A String with the <a href=
   *                             "package-summary.html#SQL-Like_Query">SQL-like
   *                             query</a> used to specify what properties to
   *                             retrieve from which repository spaces.
   * @return a {@code List<RepositorySpaceWsResult>} with the results.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public List<RepositorySpaceWsResult> queryRepositorySpaces(
      String repositorySpaceQuery) throws LockssWebServicesFault {
    log.debug2("repositorySpaceQuery = {}", repositorySpaceQuery);

    try {
      // TODO: REPLACE THIS BLOCK WITH THE ACTUAL IMPLEMENTATION.
      List<RepositorySpaceWsResult> results = new ArrayList<>();
      // TODO: END OF BLOCK TO BE REPLACED.

      log.debug2("results = {}", results);
      return results;
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }

  /**
   * Provides the selected properties of selected repositories in the system.
   * 
   * @param repositoryQuery A String with the
   *                        <a href="package-summary.html#SQL-Like_Query">
   *                        SQL-like query</a> used to specify what properties
   *                        to retrieve from which repositories.
   * @return a {@code List<RepositoryWsResult>} with the results.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public List<RepositoryWsResult> queryRepositories(String repositoryQuery)
      throws LockssWebServicesFault {
    log.debug2("repositoryQuery = {}", repositoryQuery);

    try {
      // TODO: REPLACE THIS BLOCK WITH THE ACTUAL IMPLEMENTATION.
      List<RepositoryWsResult> results = new ArrayList<>();
      // TODO: END OF BLOCK TO BE REPLACED.

      log.debug2("results = {}", results);
      return results;
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }

  /**
   * Provides the selected properties of selected crawls in the system.
   * 
   * @param crawlQuery A String with the
   *                   <a href="package-summary.html#SQL-Like_Query">SQL-like
   *                   query</a> used to specify what properties to retrieve
   *                   from which crawls.
   * @return a {@code List<CrawlWsResult>} with the results.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public List<CrawlWsResult> queryCrawls(String crawlQuery)
      throws LockssWebServicesFault {
    log.debug2("crawlQuery = {}", crawlQuery);

    try {
      // TODO: REPLACE THIS BLOCK WITH THE ACTUAL IMPLEMENTATION.
      List<CrawlWsResult> results = new ArrayList<>();
      // TODO: END OF BLOCK TO BE REPLACED.

      log.debug2("results = {}", results);
      return results;
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }

  /**
   * Provides the selected properties of selected polls in the system.
   * 
   * @param pollQuery A String with the
   *                  <a href="package-summary.html#SQL-Like_Query">SQL-like
   *                  query</a> used to specify what properties to retrieve from
   *                  which polls.
   * @return a {@code List<PollWsResult>} with the results.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public List<PollWsResult> queryPolls(String pollQuery)
      throws LockssWebServicesFault {
    log.debug2("pollQuery = {}", pollQuery);

    try {
      // TODO: REPLACE THIS BLOCK WITH THE ACTUAL IMPLEMENTATION.
      List<PollWsResult> results = new ArrayList<>();
      // TODO: END OF BLOCK TO BE REPLACED.

      log.debug2("results = {}", results);
      return results;
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }

  /**
   * Provides the platform configuration.
   * 
   * @return a PlatformConfigurationWsResult with the platform configuration.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public PlatformConfigurationWsResult getPlatformConfiguration()
      throws LockssWebServicesFault {
    log.debug2("Invoked.");

    try {
      // TODO: REPLACE THIS BLOCK WITH THE ACTUAL IMPLEMENTATION.
      PlatformConfigurationWsResult result =
	  new PlatformConfigurationWsResult();
      // TODO: END OF BLOCK TO BE REPLACED.

      log.debug2("result = {}", result);
      return result;
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }

  /**
   * Provides the selected properties of selected title database publishers.
   * 
   * @param tdbPublisherQuery A String with the <a href=
   *                          "package-summary.html#SQL-Like_Query">SQL-like
   *                          query</a> used to specify what properties to
   *                          retrieve from which title database publishers.
   * @return a {@code List<TdbPublisherWsResult>} with the results.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public List<TdbPublisherWsResult> queryTdbPublishers(String tdbPublisherQuery)
      throws LockssWebServicesFault {
    log.debug2("tdbPublisherQuery = {}", tdbPublisherQuery);

    try {
      // TODO: REPLACE THIS BLOCK WITH THE ACTUAL IMPLEMENTATION.
      List<TdbPublisherWsResult> results = new ArrayList<>();
      // TODO: END OF BLOCK TO BE REPLACED.

      log.debug2("results = {}", results);
      return results;
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }

  /**
   * Provides the selected properties of selected title database titles.
   * 
   * @param tdbTitleQuery A String with the
   *                      <a href="package-summary.html#SQL-Like_Query">SQL-like
   *                      query</a> used to specify what properties to retrieve
   *                      from which title database titles.
   * @return a {@code List<TdbTitleWsResult>} with the results.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public List<TdbTitleWsResult> queryTdbTitles(String tdbTitleQuery)
      throws LockssWebServicesFault {
    log.debug2("tdbTitleQuery = {}", tdbTitleQuery);

    try {
      // TODO: REPLACE THIS BLOCK WITH THE ACTUAL IMPLEMENTATION.
      List<TdbTitleWsResult> results = new ArrayList<>();
      // TODO: END OF BLOCK TO BE REPLACED.

      log.debug2("results = {}", results);
      return results;
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }

  /**
   * Provides the selected properties of selected title database archival units.
   * 
   * @param tdbAuQuery A String with the
   *                   <a href="package-summary.html#SQL-Like_Query">SQL-like
   *                   query</a> used to specify what properties to retrieve
   *                   from which title database archival units.
   * @return a {@code List<TdbAuWsResult>} with the results.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public List<TdbAuWsResult> queryTdbAus(String tdbAuQuery)
      throws LockssWebServicesFault {
    log.debug2("tdbAuQuery = {}", tdbAuQuery);

    try {
      // TODO: REPLACE THIS BLOCK WITH THE ACTUAL IMPLEMENTATION.
      List<TdbAuWsResult> results = new ArrayList<>();
      // TODO: END OF BLOCK TO BE REPLACED.

      log.debug2("results = {}", results);
      return results;
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }

  /**
   * Provides the URLs in an archival unit.
   * 
   * @param auId A String with the identifier of the archival unit.
   * @param url  A String with the URL above which no results will be provided,
   *             or <code>NULL</code> if all the URLS are to be provided.
   * @return a {@code List<String>} with the results.
   * @throws LockssWebServicesFault if there are problems.
   */
  public List<String> getAuUrls(String auId, String url)
      throws LockssWebServicesFault {
    log.debug2("auId = {}, url = {}", auId, url);

    try {
      // TODO: REPLACE THIS BLOCK WITH THE ACTUAL IMPLEMENTATION.
      List<String> results = new ArrayList<>();
      // TODO: END OF BLOCK TO BE REPLACED.

      log.debug2("results = {}", results);
      return results;
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }
}
