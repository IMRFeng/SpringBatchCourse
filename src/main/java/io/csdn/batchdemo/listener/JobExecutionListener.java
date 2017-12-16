package io.csdn.batchdemo.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Zhantao Feng.
 */
@Component
@Slf4j
public class JobExecutionListener extends JobExecutionListenerSupport {

    private StopWatch stopWatch;

    @Override public void beforeJob(JobExecution jobExecution) {
        stopWatch = new StopWatch();
        stopWatch.start("处理 " + jobExecution.getJobInstance().getJobName() + " 中......");
    }

    @Override public void afterJob(JobExecution jobExecution) {

        stopWatch.stop();

        long duration = stopWatch.getLastTaskTimeMillis();

        String jobName = jobExecution.getJobInstance().getJobName();

        log.info(String.format("作业 " + jobName +  " 已完成，用时: %d 分 %d 秒.", TimeUnit.MILLISECONDS.toMinutes(duration),
                TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
        ));

        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            log.info(jobName + "已成功执行完.");

        } else if (jobExecution.getStatus() == BatchStatus.FAILED) {
            log.info("执行" + jobName + "失败，异常内容如下: ");
            List<Throwable> exceptionList = jobExecution.getAllFailureExceptions();
            for (Throwable th : exceptionList) {
                log.info("异常: " + th.getLocalizedMessage());
            }
        }
    }
}