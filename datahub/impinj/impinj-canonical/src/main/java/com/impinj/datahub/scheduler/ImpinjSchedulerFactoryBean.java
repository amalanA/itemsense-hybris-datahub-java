package com.impinj.datahub.scheduler;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import com.impinj.datahub.config.DynamicConfig;
import com.impinj.datahub.constants.ImpinjDatahubConstants;

/**
 * Class responsible of creating multiple quartz jobs based on configuration.
 */
public class ImpinjSchedulerFactoryBean extends SchedulerFactoryBean
{
	private static final Logger LOGGER = LoggerFactory.getLogger(ImpinjSchedulerFactoryBean.class);
	private final DynamicConfig config = DynamicConfig.getConfig();

	@Override
	public void afterPropertiesSet() throws Exception
	{
		try
		{
			// load triggers from properties file and create jobs
			final Integer jobs = Integer.parseInt(config.getProperty(ImpinjDatahubConstants.CONFIG_JOBS));
			if (jobs > 0)
			{
				final Scheduler scheduler = new StdSchedulerFactory().getScheduler();
				for (Integer i = 1; i <= jobs; i++)
				{
					final JobKey jobKey = new JobKey(i.toString(), ImpinjDatahubConstants.CONFIG_JOBS);
					final JobDetail job = JobBuilder
							.newJob(ImpinjScheduledJob.class)
							.withIdentity(jobKey)
							.usingJobData(
									ImpinjDatahubConstants.CONFIG_USERNAME,
									config.getProperty(ImpinjDatahubConstants.CONFIG_JOBS + "." + i + "."
											+ ImpinjDatahubConstants.CONFIG_USERNAME))
							.usingJobData(
									ImpinjDatahubConstants.CONFIG_PASSWORD,
									config.getProperty(ImpinjDatahubConstants.CONFIG_JOBS + "." + i + "."
											+ ImpinjDatahubConstants.CONFIG_PASSWORD))
							.usingJobData(
									ImpinjDatahubConstants.CONFIG_ENDPOINT_URL,
									config.getProperty(ImpinjDatahubConstants.CONFIG_JOBS + "." + i + "."
											+ ImpinjDatahubConstants.CONFIG_ENDPOINT_URL))
							.usingJobData(
									ImpinjDatahubConstants.CONFIG_EPC_PREFIX,
									config.getProperty(ImpinjDatahubConstants.CONFIG_JOBS + "." + i + "."
											+ ImpinjDatahubConstants.CONFIG_EPC_PREFIX))
							.usingJobData(
									ImpinjDatahubConstants.CONFIG_WAREHOUSE,
									config.getProperty(ImpinjDatahubConstants.CONFIG_JOBS + "." + i + "."
											+ ImpinjDatahubConstants.CONFIG_WAREHOUSE))
							.usingJobData(
									ImpinjDatahubConstants.CONFIG_MASTER_DATA_FILE,
									config.getProperty(ImpinjDatahubConstants.CONFIG_JOBS + "." + i + "."
											+ ImpinjDatahubConstants.CONFIG_MASTER_DATA_FILE))
							.usingJobData(
									ImpinjDatahubConstants.CONFIG_LOOKBACK_WINDOW_IN_SECONDS,
									config.getProperty(ImpinjDatahubConstants.CONFIG_JOBS + "." + i + "."
											+ ImpinjDatahubConstants.CONFIG_LOOKBACK_WINDOW_IN_SECONDS))
							.usingJobData(
									ImpinjDatahubConstants.CONFIG_FACILITY,
									config.getProperty(ImpinjDatahubConstants.CONFIG_JOBS + "." + i + "."
											+ ImpinjDatahubConstants.CONFIG_FACILITY))
							.usingJobData(
									ImpinjDatahubConstants.CONFIG_ZONES,
									config.getProperty(ImpinjDatahubConstants.CONFIG_JOBS + "." + i + "."
											+ ImpinjDatahubConstants.CONFIG_ZONES))
							.build();

					final Trigger trigger = TriggerBuilder
							.newTrigger()
							.withIdentity("trigger" + i, ImpinjDatahubConstants.CONFIG_JOBS)
							.withSchedule(
									CronScheduleBuilder.cronSchedule(config.getProperty(ImpinjDatahubConstants.CONFIG_JOBS + "." + i + "."
											+ ImpinjDatahubConstants.CONFIG_CRON_EXPRESSION))).build();

					scheduler.scheduleJob(job, trigger);
				}

				scheduler.start();
			}
			else
			{
				LOGGER.warn("No jobs configured for Impinj");
			}
		}
		catch (final NumberFormatException e)
		{
			LOGGER.error("Number required for jobs");
		}
		catch (final Exception e)
		{
			LOGGER.error("Error while processing jobs " + e.getMessage());
			e.printStackTrace();
		}
	}

}
