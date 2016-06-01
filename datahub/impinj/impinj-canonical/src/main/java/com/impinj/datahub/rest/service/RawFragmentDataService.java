package com.impinj.datahub.rest.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.util.CollectionUtils;

import com.hybris.datahub.dto.integration.RawFragmentData;

/**
 * Service class responsible of getting data from input and transform to raw data.
 * 
 * @deprecated used for load data via rest api
 */
@Deprecated
public class RawFragmentDataService
{

	private static final Logger logger = LoggerFactory.getLogger(RawFragmentDataService.class);
	private static final String JSON_MAP_KEY_ITEMS = "items";
	private static final String EXTENSION_SOURCE = "my extension source";

	private MessageChannel rawFragmentDataInputChannel;

	@Required
	public void setRawFragmentDataInputChannel(final MessageChannel rawFragmentDataInputChannel)
	{
		this.rawFragmentDataInputChannel = rawFragmentDataInputChannel;
	}

	public void loadRawDataFragment(final String feedName, final String type, final InputStream in)
	{
		rawFragmentDataInputChannel.send(new GenericMessage(createRawFragments(feedName, type, in)));
	}

	private List<RawFragmentData> createRawFragments(final String feedName, final String type, final InputStream in)
	{

		final List<RawFragmentData> result = new ArrayList<>();
		final ObjectMapper mapper = new ObjectMapper();

		Map<String, Object> jsonMap = new HashMap<>();
		try
		{
			jsonMap = mapper.readValue(in, Map.class);
		}
		catch (final IOException e)
		{
			logger.error(e.getMessage());
		}

		if (jsonMap.containsKey(JSON_MAP_KEY_ITEMS))
		{
			for (final Map.Entry<String, Object> entry : jsonMap.entrySet())
			{
				if (entry.getKey().equalsIgnoreCase(JSON_MAP_KEY_ITEMS))
				{
					if (entry.getValue() instanceof ArrayList)
					{
						final List list = (ArrayList) entry.getValue();
						if (!CollectionUtils.isEmpty(list))
						{
							for (final Object obj : list)
							{
								final RawFragmentData rawFragmentData = new RawFragmentData();
								createFragmentData(feedName, type, obj, rawFragmentData);
								result.add(rawFragmentData);
							}
						}
					}
				}
			}
		}
		return result;
	}

	private void createFragmentData(final String feedName, final String type, final Object object,
			final RawFragmentData rawFragmentData)
	{

		rawFragmentData.setType(type);
		rawFragmentData.setDataFeedName(feedName);
		rawFragmentData.setExtensionSource(EXTENSION_SOURCE);

		final Map<String, String> valueMap = (HashMap) object;
		rawFragmentData.setValueMap(valueMap);
	}

}
