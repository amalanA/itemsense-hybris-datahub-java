package com.impinj.datahub.scheduler;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Class responsible of create spring context to get specific bean.
 */
public class SpringContext implements ApplicationContextAware
{
	private static ApplicationContext applicationContext;

	public void setApplicationContext(final ApplicationContext context) throws BeansException
	{
		this.applicationContext = context;
	}

	/**
	 * @return the context
	 */
	public static ApplicationContext getApplicationContext()
	{
		return applicationContext;

	}
}
