package com.impinj.datahub.event;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.datahub.api.event.CompositionCompletedEvent;
import com.hybris.datahub.api.event.DataHubEventListener;
import com.hybris.datahub.api.event.InitiatePublicationEvent;
import com.hybris.datahub.service.EventPublicationService;
import com.hybris.datahub.service.impl.AbstractPoolEventListener;
import com.impinj.datahub.constants.ImpinjDatahubConstants;

/**
 * Listener that handles the data composed and the publication.
 */
public class CompositionCompletedEventListener extends AbstractPoolEventListener implements
		DataHubEventListener<CompositionCompletedEvent>
{
	private static final Logger logger = LoggerFactory.getLogger(CompositionCompletedEventListener.class);
	private static final String[] TARGET_SYSTEMS = new String[] { ImpinjDatahubConstants.DATAHUB_TARGET_SYSTEM };

	private EventPublicationService eventPublicationService;

	@Override
	public void handleEvent(final CompositionCompletedEvent event)
	{
		final String poolName = getPoolNameFromId(event.getPoolId());
		if (ImpinjDatahubConstants.DATAHUB_POOL.equals(poolName))
		{
			logger.debug("Handling composition completed event for pool : {}", ImpinjDatahubConstants.DATAHUB_POOL);
			final InitiatePublicationEvent publishEvent = new InitiatePublicationEvent(event.getPoolId(),
					Arrays.asList(TARGET_SYSTEMS));
			eventPublicationService.publishEvent(publishEvent);
		}
	}

	@Override
	public Class<CompositionCompletedEvent> getEventClass()
	{
		return CompositionCompletedEvent.class;
	}

	@Override
	public boolean executeInTransaction()
	{
		return true;
	}

	/**
	 * @param eventPublicationService the the event publication service
	 */
	@Required
	public void setEventPublicationService(final EventPublicationService eventPublicationService)
	{
		this.eventPublicationService = eventPublicationService;
	}
}
