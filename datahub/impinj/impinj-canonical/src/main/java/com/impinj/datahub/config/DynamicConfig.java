package com.impinj.datahub.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import com.impinj.datahub.constants.ImpinjDatahubConstants;

/**
 * Configuration properties utility class.
 */
public class DynamicConfig
{

	private static final Logger LOGGER = LoggerFactory.getLogger(DynamicConfig.class);
	private static DynamicConfig instance;

	/**
	 * @return the DynamicConfig instance.
	 */
	public static DynamicConfig getConfig()
	{
		if (instance == null)
		{
			instance = new DynamicConfig();
		}
		return instance;
	}

	/**
	 * Method to get a specific property from properties file.
	 *
	 * @param key the key
	 * @return the value associated to the key
	 * @throws IOException
	 */
	public String getProperty(final String key) throws IOException
	{
		InputStream inputStream = null;
		try
		{
			final Properties prop = new Properties();

			inputStream = getClass().getClassLoader().getResourceAsStream(ImpinjDatahubConstants.PROPERTIES_FILENAME);

			if (inputStream != null)
			{
				prop.load(inputStream);
			}
			else
			{
				throw new FileNotFoundException("property file '" + ImpinjDatahubConstants.PROPERTIES_FILENAME + "' not found in the classpath");
			}

			return prop.getProperty(key);
		}
		catch (final Exception e)
		{
			Throwables.propagate(e);
			LOGGER.error("Error while getting properties: " + e);
		}
		finally
		{
			if (inputStream != null)
			{
				inputStream.close();
			}
		}
		return StringUtils.EMPTY;
	}

	/**
	 * Instantiates a new DynamicConfig instance.
	 */
	private DynamicConfig()
	{
		// not called
	}

}
