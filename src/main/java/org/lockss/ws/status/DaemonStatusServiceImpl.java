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

import static org.lockss.ws.SoapServiceUtil.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lockss.log.L4JLogger;
import org.lockss.util.Constants;
import org.lockss.util.rest.RestUtil;
import org.lockss.util.rest.exception.LockssRestException;
import org.lockss.util.rest.status.RestStatusClient;
import org.lockss.ws.SoapApplication;
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

/**
 * The Daemon Status SOAP web service implementation.
 */
@Service
public class DaemonStatusServiceImpl implements DaemonStatusService {
  private final static L4JLogger log = L4JLogger.getLogger();

  @Autowired
  private Environment env;

  private long connectTimeout = 10 * Constants.SECOND;
  private long readTimeout = 120 * Constants.SECOND;

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
      ResponseEntity<String> response = callRestServiceEndpoint(
	  env.getProperty(CONFIG_SVC_URL_KEY), "/austatuses/{auId}",
	  uriVariables, null, HttpMethod.GET, "Can't get AU status");

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
      // Prepare the query parameters.
      Map<String, String> queryParams = new HashMap<>(1);
      queryParams.put("pluginQuery", pluginQuery);
      log.trace("queryParams = {}", queryParams);

      // Make the REST call.
      ResponseEntity<String> response = callRestServiceEndpoint(
	  env.getProperty(CONFIG_SVC_URL_KEY), "/plugins", null, queryParams,
	  HttpMethod.GET, "Can't query plugins");

      // Get the response body.
      try {
        ObjectMapper mapper = new ObjectMapper();
        List<PluginWsResult> result = mapper.readValue(response.getBody(),
  	  new TypeReference<List<PluginWsResult>>(){});

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
      // Prepare the query parameters.
      Map<String, String> queryParams = new HashMap<>(1);
      queryParams.put("auQuery", auQuery);
      log.trace("queryParams = {}", queryParams);

      // Make the REST call.
      ResponseEntity<String> response = callRestServiceEndpoint(
	  env.getProperty(CONFIG_SVC_URL_KEY), "/auqueries", null, queryParams,
	  HttpMethod.GET, "Can't query AUs");

      // Get the response body.
      try {
        ObjectMapper mapper = new ObjectMapper();
        List<AuWsResult> result = mapper.readValue(response.getBody(),
  	  new TypeReference<List<AuWsResult>>(){});

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
      // Make the REST call.
      ResponseEntity<String> response = callRestServiceEndpoint(
	  env.getProperty(CONFIG_SVC_URL_KEY), "/config/platform", null, null,
	  HttpMethod.GET, "Can't get platform configuration");

      // Get the response body.
      try {
        ObjectMapper mapper = new ObjectMapper();
        PlatformConfigurationWsResult result = mapper.readValue(
            response.getBody(), PlatformConfigurationWsResult.class);

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
      // Prepare the query parameters.
      Map<String, String> queryParams = new HashMap<>(1);
      queryParams.put("tdbPublisherQuery", tdbPublisherQuery);
      log.trace("queryParams = {}", queryParams);

      // Make the REST call.
      ResponseEntity<String> response = callRestServiceEndpoint(
	  env.getProperty(CONFIG_SVC_URL_KEY), "/tdbpublishers", null,
	  queryParams, HttpMethod.GET, "Can't query TDB publishers");

      // Get the response body.
      try {
        ObjectMapper mapper = new ObjectMapper();
        List<TdbPublisherWsResult> result = mapper.readValue(response.getBody(),
  	  new TypeReference<List<TdbPublisherWsResult>>(){});

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
      // Prepare the query parameters.
      Map<String, String> queryParams = new HashMap<>(1);
      queryParams.put("tdbTitleQuery", tdbTitleQuery);
      log.trace("queryParams = {}", queryParams);

      // Make the REST call.
      ResponseEntity<String> response = callRestServiceEndpoint(
	  env.getProperty(CONFIG_SVC_URL_KEY), "/tdbtitles", null, queryParams,
	  HttpMethod.GET, "Can't query TDB titles");

      // Get the response body.
      try {
        ObjectMapper mapper = new ObjectMapper();
        List<TdbTitleWsResult> result = mapper.readValue(response.getBody(),
  	  new TypeReference<List<TdbTitleWsResult>>(){});

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
      // Prepare the query parameters.
      Map<String, String> queryParams = new HashMap<>(1);
      queryParams.put("tdbAuQuery", tdbAuQuery);
      log.trace("queryParams = {}", queryParams);

      // Make the REST call.
      ResponseEntity<String> response = callRestServiceEndpoint(
	  env.getProperty(CONFIG_SVC_URL_KEY), "/tdbaus", null, queryParams,
	  HttpMethod.GET, "Can't query TDB AUs");

      // Get the response body.
      try {
        ObjectMapper mapper = new ObjectMapper();
        List<TdbAuWsResult> result = mapper.readValue(response.getBody(),
  	  new TypeReference<List<TdbAuWsResult>>(){});

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

  /**
   * Makes a call to a REST service endpoint.
   * 
   * @param serviceUrl       A String with the URL of the service.
   * @param endPointPath     A String with the URI path to the endpoint.
   * @param uriVariables     A Map<String, String> with any variables to be
   *                         interpolated in the URI.
   * @param queryParams      A Map<String, String> with any query parameters.
   * @param httpMethod       An HttpMethod with HTTP method used to make the
   *                         call to the REST service,
   * @param exceptionMessage A String with the message to be returned with any
   *                         exception.
   * @return a ResponseEntity<String> with the response from the REST service.
   * @throws LockssRestException if any problems arise in the call to the REST
   *                             service.
   */
  private ResponseEntity<String> callRestServiceEndpoint(String serviceUrl, 
      String endPointPath, Map<String, String> uriVariables,
      Map<String, String> queryParams, HttpMethod httpMethod,
      String exceptionMessage) throws LockssRestException {
    log.debug2("serviceUrl = {}", serviceUrl);
    log.debug2("endPointPath = {}", endPointPath);
    log.debug2("uriVariables = {}", uriVariables);
    log.debug2("queryParams = {}", queryParams);
    log.debug2("httpMethod = {}", httpMethod);
    log.debug2("exceptionMessage = {}", exceptionMessage);

    // Create the REST template.
    RestTemplate restTemplate =
	  RestUtil.getRestTemplate(connectTimeout, readTimeout);

    // Create the URI of the request to the REST service.
    String uriString = serviceUrl + endPointPath;
    log.trace("uriString = {}", uriString);


    URI uri = RestUtil.getRestUri(uriString, uriVariables, queryParams);
    log.trace("uri = {}", uri);

    // Initialize the request headers.
    List<String> restClientCredentials =
	  SoapApplication.getRestClientCredentials();

    HttpHeaders requestHeaders =
	  RestUtil.getCredentialedRequestHeaders(restClientCredentials.get(0),
	      restClientCredentials.get(1));

    // Create the request entity.
    HttpEntity<Void> requestEntity =
	new HttpEntity<>(null, requestHeaders);

    // Make the REST call.
    log.trace("Calling RestUtil.callRestService");
    return RestUtil.callRestService(restTemplate, uri, httpMethod,
	requestEntity, String.class, exceptionMessage);
  }
}
