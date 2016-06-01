package com.impinj.datahub.config;

import com.impinj.datahub.itemsense.ItemSenseConfiguration;

/**
 * Class responsible of returning ImpinjConfiguration to connect to Impinj.
 */
public class ImpinjConfiguration
{

	/**
	 * Gets impinj configuration
	 *
	 * @param baseUrl
	 * @param userName
	 * @param password
	 * @return impinj configuration
	 */
	public static ItemSenseConfiguration getConfiguration(final String baseUrl, final String userName, final String password)
	{
		final ItemSenseConfiguration configuration = new ItemSenseConfiguration();
		configuration.setBaseUrl(baseUrl);
		configuration.setUserName(userName);
		configuration.setPassword(password);
		return configuration;
	}

	/**
	 * Instantiates a new ImpinjConfiguration instance.
	 */
	private ImpinjConfiguration()
	{
		// not called
	}

}
