package com.impinj.datahub.rest;

import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Required;

import com.hybris.datahub.validation.ValidationException;
import com.impinj.datahub.rest.service.RawFragmentDataService;

/**
 * REST API that provides a test method to import data to datahub.
 *
 * @deprecated used for testing purposes only
 */
@Path(value = "/data-feeds/{feedName}/items-test/{type}")
@Consumes(value = { "application/json" })
@Deprecated
public class RawFramgentDataRestService
{
	private RawFragmentDataService rawFragmentDataService;

	/**
	 * Test method to load data
	 *
	 * @param feedName
	 * @param type
	 * @param in
	 * @return the response status
	 * @throws ValidationException
	 */
	@POST
	public Response getTest(@PathParam(value = "feedName") final String feedName, @PathParam(value = "type") final String type,
			final InputStream in) throws ValidationException
	{
		rawFragmentDataService.loadRawDataFragment(feedName, type, in);
		return Response.ok().build();
	}

	/**
	 * @param rawFragmentDataService
	 */
	@Required
	public void setRawFragmentDataService(final RawFragmentDataService rawFragmentDataService)
	{
		this.rawFragmentDataService = rawFragmentDataService;
	}

}
