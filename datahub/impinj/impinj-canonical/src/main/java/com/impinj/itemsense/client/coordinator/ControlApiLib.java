/**
 * IMPINJ CONFIDENTIAL AND PROPRIETARY
 *
 * This source code is the sole property of Impinj, Inc. Reproduction or utilization of this source
 * code in whole or in part is forbidden without the prior written consent of Impinj, Inc.
 *
 * (c) Copyright Impinj, Inc. 2015. All rights reserved.
 */

// This class is built on top of generated code. See the comments in Localhost_ItemsenseCoordinator
// for
// instructions if you need to re-generate that code.
package com.impinj.itemsense.client.coordinator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

import lombok.extern.log4j.Log4j;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


import com.impinj.itemsense.client.coordinator.job.Job;
import com.impinj.itemsense.client.coordinator.job.JobResponse;
import com.impinj.itemsense.client.coordinator.job.JobResponseError;
import com.impinj.itemsense.client.coordinator.job.JobsResponse;
import com.impinj.itemsense.client.coordinator.job.JobStatus;
import com.impinj.itemsense.client.coordinator.job.JobStopReason;

@Log4j
public class ControlApiLib {

  private final Gson gson;
  private final WebResource target;

  /**
   * Constructor
   *
   * @param uri the base URI for the items service
   */
  public ControlApiLib(final Gson gson, final Client client, final URI uri) {
    this.gson = gson;
    target = client.resource(uri);
  }

  public boolean anyJobIsRunning() throws IOException {
    return showJobs().stream().anyMatch(job -> job.getStatus() == JobStatus.RUNNING);
  }


  /**
   * Queries the Control service for all jobs
   */
  public Collection<JobsResponse> showJobs() throws IOException {
    final String response =
        target.path("control/jobs/show").accept(MediaType.APPLICATION_JSON_TYPE).get(String.class);
    log.debug("/jobs/show response: " + response);
    return gson.fromJson(response, new TypeToken<Collection<JobsResponse>>() {}.getType());
  }

  public JobsResponse showJobId(final String jobId) throws IOException {
    log.debug("About to show job");
    final String response = target.path("control/jobs/show/" + jobId)
        .accept(MediaType.APPLICATION_JSON_TYPE).get(String.class);
    log.debug("/jobs/show/{jobId} response: " + response);
    return gson.fromJson(response, JobsResponse.class);
  }
}

