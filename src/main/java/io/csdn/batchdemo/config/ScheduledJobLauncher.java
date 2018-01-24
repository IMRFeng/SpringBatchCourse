package io.csdn.batchdemo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Date;

/**
 * @author Zhantao Feng.
 */
@Configuration
@ConditionalOnProperty(value = "io.cron.job.enabled")
public class ScheduledJobLauncher {

    private Logger logger = LoggerFactory.getLogger(ScheduledJobLauncher.class);

    private JobLauncher jobLauncher;

    private Job anotherJob;

    public ScheduledJobLauncher(JobLauncher jobLauncher,
                                @Qualifier("anotherJob") Job anotherJob) {
        this.jobLauncher = jobLauncher;
        this.anotherJob = anotherJob;
    }

    @Scheduled(cron = "${io.cron.job:0 0 4 * * ?}")
    public void perform() throws Exception {
        Date currentDate = new Date();
        logger.info("Scheduled cron job started at : " + currentDate);

        JobParameters param = new JobParametersBuilder().addDate("date", currentDate)
                .addString("type-code", "triggered by cron job").toJobParameters();
        JobExecution execution = jobLauncher.run(this.anotherJob, param);

        if (execution.getStatus().isUnsuccessful()) {
            logger.error("Failed to run the scheduled cron job, exceptions are : " + execution.getFailureExceptions());
        }
    }
}
