package io.csdn.batchdemo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;

/**
 * @author Zhantao Feng.
 */
@RestController
public class BatchStartupController {
    private static final String MAC_EPG_IMPORTER_GENERAL_ERROR_CODE = "BATCH_JOB_ERR-10000";
    private final Logger LOGGER = LoggerFactory.getLogger(BatchStartupController.class);

    /** The job launcher. */
    private JobLauncher jobLauncher;

    /** The mac ScheduleEvent job. */
    @Qualifier("anotherJob")
    private Job anotherJob;

    public BatchStartupController(JobLauncher jobLauncher, Job batchDemoJob) {
        this.jobLauncher = jobLauncher;
        this.anotherJob = batchDemoJob;
    }

    @GetMapping("/launchBatchDemoJob")
    public ResponseEntity<String> batchDemoJob(@RequestParam(value = "parameter", required = false) String parameter) throws JobRestartException,
                                                    JobInstanceAlreadyCompleteException,
                                                    JobParametersInvalidException {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("parameter", parameter)
                .addLong("time", System.currentTimeMillis()).toJobParameters();

        try {
            JobExecution execution = jobLauncher.run(anotherJob, jobParameters);

            if (ExitStatus.COMPLETED.getExitCode().equals(execution.getExitStatus().getExitCode())) {
                return new ResponseEntity<>("Completed", HttpStatus.OK);
            } else if (ExitStatus.NOOP.getExitCode().equals(execution.getExitStatus().getExitCode())) {
                return new ResponseEntity<String>(execution.getExitStatus().getExitDescription(), HttpStatus.BAD_REQUEST);
            } else {
                final List<Throwable> exceptions = execution.getAllFailureExceptions();
                LOGGER.error("{} - the job {} of batchDemoJob was failed with errors {}",
                        MAC_EPG_IMPORTER_GENERAL_ERROR_CODE, execution.getJobId(), exceptions);
                return new ResponseEntity<>("Oops, something went wrong. Please try again later.", HttpStatus.INTERNAL_SERVER_ERROR);
            }

        } catch (JobExecutionAlreadyRunningException e) {
            LOGGER.warn("The job of batchDemoJob is still in running.");
            return new ResponseEntity<>("The last job execution is still in running, please try again later", HttpStatus.CONFLICT);
        }
    }

    @ExceptionHandler({ JobRestartException.class, JobInstanceAlreadyCompleteException.class,
            JobParametersInvalidException.class, SkipLimitExceededException.class, ResourceAccessException.class})
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public @ResponseBody String handleExceptions(Exception ex) {
        LOGGER.error("{} - the job running of batchDemoJob is failed with {}",
                MAC_EPG_IMPORTER_GENERAL_ERROR_CODE, ex.getMessage());
        return "Oops, something went wrong. Please try again later.";
    }
}
