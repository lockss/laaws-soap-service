/*

Copyright (c) 2013-2020 Board of Trustees of Leland Stanford Jr. University,
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.lockss.app.*;
import org.lockss.laaws.rs.model.Artifact;
import org.lockss.log.L4JLogger;
import org.lockss.util.rest.exception.LockssRestException;
import org.lockss.util.rest.poller.RestPollerClient;
import org.lockss.util.rest.status.RestStatusClient;
import org.lockss.ws.BaseServiceImpl;
import org.lockss.ws.entities.*;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

/** The Daemon Status SOAP web service implementation. */
@Service
public class DaemonStatusServiceImpl extends BaseServiceImpl implements DaemonStatusService {
  private static final L4JLogger log = L4JLogger.getLogger();

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
      String url = getServiceEndpoint(ServiceDescr.SVC_REPO);

      if (url != null) {
        result = isServiceReady(url);

        if (!result) {
          log.debug2("result = {}", result);
          return result;
        }
      }

      // Check the configuration service.
      url = getServiceEndpoint(ServiceDescr.SVC_CONFIG);

      if (url != null) {
        result = isServiceReady(url);

        if (!result) {
          log.debug2("result = {}", result);
          return result;
        }
      }

      // Check the poller service.
      url = getServiceEndpoint(ServiceDescr.SVC_POLLER);

      if (url != null) {
        result = isServiceReady(url);

        if (!result) {
          log.debug2("result = {}", result);
          return result;
        }
      }

      // Check the metadata extractor service.
      url = getServiceEndpoint(ServiceDescr.SVC_MDX);

      if (url != null) {
        result = isServiceReady(url);

        if (!result) {
          log.debug2("result = {}", result);
          return result;
        }
      }

      // Check the metadata service.
      url = getServiceEndpoint(ServiceDescr.SVC_MDQ);

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
      isReady = new RestStatusClient(serviceUrl)
          .setRestTemplate(restTemplate)
          .getStatus()
          .isReady();
    } catch (LockssRestException lre) {
      log.debug("Ignored exception caught getting status of " + serviceUrl, lre);
    }

    log.debug2("isReady = {}", isReady);
    return isReady;
  }

  /**
   * Provides a list of the identifier/name pairs of the archival units in the system.
   *
   * @return a {@code List<IdNamePair>} with the identifier/name pairs of the archival units in the
   *     system.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public Collection<IdNamePair> getAuIds() throws LockssWebServicesFault {
    log.debug2("Invoked.");

    // Get the requested information via the queryAus SOAP operation.
    List<AuWsResult> queryAusResult = queryAus("select auId, name");
    log.trace("queryAusResult = {}", queryAusResult);

    try {
      // Initialize the results.
      Collection<IdNamePair> results = new ArrayList<>(queryAusResult.size());

      // Loop through the results of the queryAus SOAP operation.
      for (AuWsResult auWsResult : queryAusResult) {
        // Populate the results.
        results.add(new IdNamePair(auWsResult.getAuId(), auWsResult.getName()));
      }

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
      // Prepare the URI path variables.
      Map<String, String> uriVariables = new HashMap<>(1);
      uriVariables.put("auId", auId);
      log.trace("uriVariables = {}", uriVariables);

      // Make the REST call.
      ResponseEntity<String> response =
          callRestServiceEndpoint(
              getServiceEndpoint(ServiceDescr.SVC_CONFIG),
              "/austatuses/{auId}",
              uriVariables,
              null,
              HttpMethod.GET,
              (Void) null,
              "Can't get AU status");

      // Get the response body.
      try {
        ObjectMapper mapper = new ObjectMapper();
        AuStatus result = mapper.readValue(response.getBody(), AuStatus.class);

        log.debug2("result = " + result);
        return result;
      } catch (Exception e) {
        log.error("Cannot get body of response", e);
        throw e;
      }
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }

  /**
   * Provides the selected properties of selected plugins in the system.
   *
   * @param pluginQuery A String with the <a href="package-summary.html#SQL-Like_Query">SQL-like
   *     query</a> used to specify what properties to retrieve from which plugins.
   * @return a {@code List<PluginWsResult>} with the results.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public List<PluginWsResult> queryPlugins(String pluginQuery) throws LockssWebServicesFault {
    log.debug2("pluginQuery = {}", pluginQuery);

    try {
      // Prepare the query parameters.
      Map<String, String> queryParams = new HashMap<>(1);
      queryParams.put("pluginQuery", pluginQuery);
      log.trace("queryParams = {}", queryParams);

      // Make the REST call.
      ResponseEntity<String> response =
          callRestServiceEndpoint(
              getServiceEndpoint(ServiceDescr.SVC_CONFIG),
              "/plugins",
              null,
              queryParams,
              HttpMethod.GET,
              (Void) null,
              "Can't query plugins");

      // Get the response body.
      try {
        ObjectMapper mapper = new ObjectMapper();
        List<PluginWsResult> result =
            mapper.readValue(response.getBody(), new TypeReference<List<PluginWsResult>>() {});

        log.debug2("result = " + result);
        return result;
      } catch (Exception e) {
        log.error("Cannot get body of response", e);
        throw e;
      }
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }

  /**
   * Provides the selected properties of selected archival units in the system.
   *
   * @param auQuery A String with the <a href="package-summary.html#SQL-Like_Query">SQL-like
   *     query</a> used to specify what properties to retrieve from which archival units.
   * @return a {@code List<AuWsResult>} with the results.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public List<AuWsResult> queryAus(String auQuery) throws LockssWebServicesFault {
    log.debug2("auQuery = {}", auQuery);

    try {
      // Prepare the query parameters.
      Map<String, String> queryParams = new HashMap<>(1);
      queryParams.put("auQuery", auQuery);
      log.trace("queryParams = {}", queryParams);

      // Make the REST call.
      ResponseEntity<String> response =
          callRestServiceEndpoint(
              getServiceEndpoint(ServiceDescr.SVC_CONFIG),
              "/auqueries",
              null,
              queryParams,
              HttpMethod.GET,
              (Void) null,
              "Can't query AUs");

      // Get the response body.
      try {
        ObjectMapper mapper = new ObjectMapper();
        List<AuWsResult> result =
            mapper.readValue(response.getBody(), new TypeReference<List<AuWsResult>>() {});

        log.debug2("result = " + result);
        return result;
      } catch (Exception e) {
        log.error("Cannot get body of response", e);
        throw e;
      }
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }

  /**
   * Provides the selected properties of selected peers in the system.
   *
   * @param peerQuery A String with the <a href="package-summary.html#SQL-Like_Query">SQL-like
   *     query</a> used to specify what properties to retrieve from which peers.
   * @return a {@code List<PeerWsResult>} with the results.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public List<PeerWsResult> queryPeers(String peerQuery) throws LockssWebServicesFault {
    log.debug2("peerQuery = {}", peerQuery);

    try {
      // Make the REST call to make the query.
      List<PeerWsResult> results =
        new RestPollerClient(getServiceEndpoint(ServiceDescr.SVC_POLLER))
              .addRequestHeaders(getAuthHeaders())
              .setRestTemplate(restTemplate)
              .queryPeers(peerQuery);

      log.debug2("results = {}", results);
      return results;
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }

  /**
   * Provides the selected properties of selected votes in the system.
   *
   * @param voteQuery A String with the <a href="package-summary.html#SQL-Like_Query">SQL-like
   *     query</a> used to specify what properties to retrieve from which votes.
   * @return a {@code List<VoteWsResult>} with the results.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public List<VoteWsResult> queryVotes(String voteQuery) throws LockssWebServicesFault {
    log.debug2("voteQuery = {}", voteQuery);

    try {
      // Make the REST call to make the query.
      List<VoteWsResult> results =
          new RestPollerClient(getServiceEndpoint(ServiceDescr.SVC_POLLER))
              .addRequestHeaders(getAuthHeaders())
              .setRestTemplate(restTemplate)
              .queryVotes(voteQuery);

      log.debug2("results = {}", results);
      return results;
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }

  /**
   * Provides the selected properties of selected repository spaces in the system.
   *
   * @param repositorySpaceQuery A String with the <a href=
   *     "package-summary.html#SQL-Like_Query">SQL-like query</a> used to specify what properties to
   *     retrieve from which repository spaces.
   * @return a {@code List<RepositorySpaceWsResult>} with the results.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public List<RepositorySpaceWsResult> queryRepositorySpaces(String repositorySpaceQuery)
      throws LockssWebServicesFault {
    log.debug2("repositorySpaceQuery = {}", repositorySpaceQuery);

    try {
      // Make the REST call to make the query.
      List<RepositorySpaceWsResult> results =
          new RestPollerClient(getServiceEndpoint(ServiceDescr.SVC_POLLER))
              .addRequestHeaders(getAuthHeaders())
              .setRestTemplate(restTemplate)
              .queryRepositorySpaces(repositorySpaceQuery);

      log.debug2("results = {}", results);
      return results;
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }

  /**
   * Provides the selected properties of selected repositories in the system.
   *
   * @param repositoryQuery A String with the <a href="package-summary.html#SQL-Like_Query">SQL-like
   *     query</a> used to specify what properties to retrieve from which repositories.
   * @return a {@code List<RepositoryWsResult>} with the results.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public List<RepositoryWsResult> queryRepositories(String repositoryQuery)
      throws LockssWebServicesFault {
    log.debug2("repositoryQuery = {}", repositoryQuery);

    try {
      // Make the REST call to make the query.
      List<RepositoryWsResult> results =
           new RestPollerClient(getServiceEndpoint(ServiceDescr.SVC_POLLER))
              .addRequestHeaders(getAuthHeaders())
              .setRestTemplate(restTemplate)
              .queryAuRepositories(repositoryQuery);

      log.debug2("results = {}", results);
      return results;
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }

  /**
   * Provides the selected properties of selected crawls in the system.
   *
   * @param crawlQuery A String with the <a href="package-summary.html#SQL-Like_Query">SQL-like
   *     query</a> used to specify what properties to retrieve from which crawls.
   * @return a {@code List<CrawlWsResult>} with the results.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public List<CrawlWsResult> queryCrawls(String crawlQuery) throws LockssWebServicesFault {
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
   * @param pollQuery A String with the <a href="package-summary.html#SQL-Like_Query">SQL-like
   *     query</a> used to specify what properties to retrieve from which polls.
   * @return a {@code List<PollWsResult>} with the results.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public List<PollWsResult> queryPolls(String pollQuery) throws LockssWebServicesFault {
    log.debug2("pollQuery = {}", pollQuery);

    try {
      // Make the REST call to make the query.
      List<PollWsResult> results =
        new RestPollerClient(getServiceEndpoint(ServiceDescr.SVC_POLLER))
              .addRequestHeaders(getAuthHeaders())
              .setRestTemplate(restTemplate)
              .queryPolls(pollQuery);

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
  public PlatformConfigurationWsResult getPlatformConfiguration() throws LockssWebServicesFault {
    log.debug2("Invoked.");

    try {
      // Make the REST call.
      ResponseEntity<String> response =
          callRestServiceEndpoint(
              getServiceEndpoint(ServiceDescr.SVC_CONFIG),
              "/config/platform",
              null,
              null,
              HttpMethod.GET,
              (Void) null,
              "Can't get platform configuration");

      // Get the response body.
      try {
        ObjectMapper mapper = new ObjectMapper();
        PlatformConfigurationWsResult result =
            mapper.readValue(response.getBody(), PlatformConfigurationWsResult.class);

        log.debug2("result = " + result);
        return result;
      } catch (Exception e) {
        log.error("Cannot get body of response", e);
        throw e;
      }
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }

  /**
   * Provides the selected properties of selected title database publishers.
   *
   * @param tdbPublisherQuery A String with the <a href=
   *     "package-summary.html#SQL-Like_Query">SQL-like query</a> used to specify what properties to
   *     retrieve from which title database publishers.
   * @return a {@code List<TdbPublisherWsResult>} with the results.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public List<TdbPublisherWsResult> queryTdbPublishers(String tdbPublisherQuery)
      throws LockssWebServicesFault {
    log.debug2("tdbPublisherQuery = {}", tdbPublisherQuery);

    try {
      // Prepare the query parameters.
      Map<String, String> queryParams = new HashMap<>(1);
      queryParams.put("tdbPublisherQuery", tdbPublisherQuery);
      log.trace("queryParams = {}", queryParams);

      // Make the REST call.
      ResponseEntity<String> response =
          callRestServiceEndpoint(
              getServiceEndpoint(ServiceDescr.SVC_CONFIG),
              "/tdbpublishers",
              null,
              queryParams,
              HttpMethod.GET,
              (Void) null,
              "Can't query TDB publishers");

      // Get the response body.
      try {
        ObjectMapper mapper = new ObjectMapper();
        List<TdbPublisherWsResult> result =
            mapper.readValue(
                response.getBody(), new TypeReference<List<TdbPublisherWsResult>>() {});

        log.debug2("result = " + result);
        return result;
      } catch (Exception e) {
        log.error("Cannot get body of response", e);
        throw e;
      }
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }

  /**
   * Provides the selected properties of selected title database titles.
   *
   * @param tdbTitleQuery A String with the <a href="package-summary.html#SQL-Like_Query">SQL-like
   *     query</a> used to specify what properties to retrieve from which title database titles.
   * @return a {@code List<TdbTitleWsResult>} with the results.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public List<TdbTitleWsResult> queryTdbTitles(String tdbTitleQuery) throws LockssWebServicesFault {
    log.debug2("tdbTitleQuery = {}", tdbTitleQuery);

    try {
      // Prepare the query parameters.
      Map<String, String> queryParams = new HashMap<>(1);
      queryParams.put("tdbTitleQuery", tdbTitleQuery);
      log.trace("queryParams = {}", queryParams);

      // Make the REST call.
      ResponseEntity<String> response =
          callRestServiceEndpoint(
              getServiceEndpoint(ServiceDescr.SVC_CONFIG),
              "/tdbtitles",
              null,
              queryParams,
              HttpMethod.GET,
              (Void) null,
              "Can't query TDB titles");

      // Get the response body.
      try {
        ObjectMapper mapper = new ObjectMapper();
        List<TdbTitleWsResult> result =
            mapper.readValue(response.getBody(), new TypeReference<List<TdbTitleWsResult>>() {});

        log.debug2("result = " + result);
        return result;
      } catch (Exception e) {
        log.error("Cannot get body of response", e);
        throw e;
      }
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }

  /**
   * Provides the selected properties of selected title database archival units.
   *
   * @param tdbAuQuery A String with the <a href="package-summary.html#SQL-Like_Query">SQL-like
   *     query</a> used to specify what properties to retrieve from which title database archival
   *     units.
   * @return a {@code List<TdbAuWsResult>} with the results.
   * @throws LockssWebServicesFault if there are problems.
   */
  @Override
  public List<TdbAuWsResult> queryTdbAus(String tdbAuQuery) throws LockssWebServicesFault {
    log.debug2("tdbAuQuery = {}", tdbAuQuery);

    try {
      // Prepare the query parameters.
      Map<String, String> queryParams = new HashMap<>(1);
      queryParams.put("tdbAuQuery", tdbAuQuery);
      log.trace("queryParams = {}", queryParams);

      // Make the REST call.
      ResponseEntity<String> response =
          callRestServiceEndpoint(
              getServiceEndpoint(ServiceDescr.SVC_CONFIG),
              "/tdbaus",
              null,
              queryParams,
              HttpMethod.GET,
              (Void) null,
              "Can't query TDB AUs");

      // Get the response body.
      try {
        ObjectMapper mapper = new ObjectMapper();
        List<TdbAuWsResult> result =
            mapper.readValue(response.getBody(), new TypeReference<List<TdbAuWsResult>>() {});

        log.debug2("result = " + result);
        return result;
      } catch (Exception e) {
        log.error("Cannot get body of response", e);
        throw e;
      }
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }

  /**
   * Provides the URLs in an archival unit.
   *
   * @param auId A String with the identifier of the archival unit.
   * @param url A String with the URL above which no results will be provided, or <code>NULL</code>
   *     if all the URLS are to be provided.
   * @return a {@code List<String>} with the results.
   * @throws LockssWebServicesFault if there are problems.
   */
  public List<String> getAuUrls(String auId, String url) throws LockssWebServicesFault {
    log.debug2("auId = {}", auId);
    log.debug2("url = {}", url);

    try {
      List<String> results = new ArrayList<>();

      String prefixUrl = url == null ? "" : url;
      log.trace("prefixUrl = {}", prefixUrl);

      // Loop through all the artifacts in the response from the REST service.
      for (Artifact artifact :
          getRestLockssRepository()
              .getArtifactsWithPrefix(repoCollection, auId, prefixUrl)) {
        log.trace("artifact = {}", artifact);

        // Add this artifact URL to the results.
        results.add(artifact.getUri());
      }

      log.debug2("results = {}", results);
      return results;
    } catch (Exception e) {
      throw new LockssWebServicesFault(e);
    }
  }
}
