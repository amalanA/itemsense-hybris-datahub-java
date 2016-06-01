package com.impinj.datahub.config;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.impinj.datahub.constants.ImpinjDatahubConstants;

public class DynamicConfigTest
{

	private final DynamicConfig config = DynamicConfig.getConfig();

	@Test
	public void getPropertiesTest()
	{
		try
		{
			assertNotNull(config.getProperty(ImpinjDatahubConstants.CONFIG_JOBS));
			assertTrue(StringUtils.isEmpty(config.getProperty("notexist")));
		}
		catch (final IOException e)
		{
			// configuration file error
			e.printStackTrace();
		}
	}

}
