package com.impinj.datahub.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.datahub.api.event.DataHubEventListener;
import com.hybris.datahub.api.event.DataLoadingCompletedEvent;
import com.hybris.datahub.api.event.InitiateCompositionEvent;
import com.hybris.datahub.service.EventPublicationService;
import com.hybris.datahub.service.impl.AbstractPoolEventListener;
import com.impinj.datahub.constants.ImpinjDatahubConstants;

/**
 * Listener that handles the data loading event and initiates the composition.
 */
public class DataLoadedEventListener extends AbstractPoolEventListener implements DataHubEventListener<DataLoadingCompletedEvent>
{

	private static final Logger logger = LoggerFactory.getLogger(DataLoadedEventListener.class);

	private EventPublicationService eventPublicationService;

	@Override
	public void handleEvent(final DataLoadingCompletedEvent event)
	{
		final String poolName = getPoolNameFromId(event.getPoolId());
		if (ImpinjDatahubConstants.DATAHUB_POOL.equals(poolName))
		{
			logger.debug("Handling data loaded event for pool : {}", ImpinjDatahubConstants.DATAHUB_POOL);
			final InitiateCompositionEvent composeEvent = new InitiateCompositionEvent(event.getPoolId());
			eventPublicationService.publishEvent(composeEvent);
		}
	}

	@Override
	public Class<DataLoadingCompletedEvent> getEventClass()
	{
		return DataLoadingCompletedEvent.class;
	}

	@Override
	public boolean executeInTransaction()
	{
		return true;
	}

	/**
	 * @param eventPublicationService the event publication service
	 */
	@Required
	public void setEventPublicationService(final EventPublicationService eventPublicationService)
	{
		this.eventPublicationService = eventPublicationService;
	}
}
