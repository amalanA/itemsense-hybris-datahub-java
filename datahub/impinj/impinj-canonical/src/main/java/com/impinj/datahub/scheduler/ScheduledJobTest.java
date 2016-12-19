package com.impinj.datahub.scheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;

import com.hybris.datahub.dto.integration.RawFragmentData;
import com.impinj.datahub.constants.ImpinjDatahubConstants;

/**
 * Job class that creates a dummy data and sends to datahub input channel.
 * 
 * @deprecated testing data class used instead of real impinj job
 */
@Deprecated
@Component
public class ScheduledJobTest implements Job, ApplicationContextAware
{
	private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledJobTest.class);

	private static ApplicationContext applicationContext;
	private MessageChannel rawFragmentDataInputChannel;

	/**
	 * @param rawFragmentDataInputChannel the input channel
	 */
	@Required
	public void setRawFragmentDataInputChannel(final MessageChannel rawFragmentDataInputChannel)
	{
		this.rawFragmentDataInputChannel = rawFragmentDataInputChannel;
	}

	@Override
	public void execute(final JobExecutionContext context) throws JobExecutionException
	{
		LOGGER.warn ("Job test is runing " + context.getJobDetail().getKey().getName());

		if (rawFragmentDataInputChannel == null)
		{
			rawFragmentDataInputChannel = (MessageChannel) SpringContext.getApplicationContext().getBean(
				ImpinjDatahubConstants.RAW_FRAGMENT_DATA_INPUT_CHANNEL);
			rawFragmentDataInputChannel.send(new GenericMessage(createRawFragments(ImpinjDatahubConstants.DATAHUB_FEED, ImpinjDatahubConstants.DATAHUB_IMPINJ_RAW_ITEM_TYPE)));
		}

	}

	private List<RawFragmentData> createRawFragments(final String feedName, final String type)
	{
		final List<RawFragmentData> result = new ArrayList<>();

		final RawFragmentData rawFragmentData = new RawFragmentData();

		final Map<String, String> line = new HashMap<>();
		line.put("productId", "100124");
		line.put("stockLevel", "100");
		line.put("warehouseId", "ap_warehouse_e");

		rawFragmentData.setValueMap(line);
		rawFragmentData.setType(type);
		rawFragmentData.setDataFeedName(feedName);
		rawFragmentData.setExtensionSource("my extension source");

		result.add(rawFragmentData);

		return result;

	}

	public void setApplicationContext(final ApplicationContext context) throws BeansException
	{
		this.applicationContext = context;
	}

	/**
	 * @return the application context
	 */
	public static ApplicationContext getApplicationContext()
	{
		return applicationContext;

	}

}
