package io.csdn.batchdemo;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;

/**
 * @author Zhantao Feng on 19/11/17.
 */
public class StepBooleanDecider implements JobExecutionDecider {
    private boolean flag;

    public StepBooleanDecider(){}

    public StepBooleanDecider(boolean flag) {
        this.flag = flag;
    }

    @Override
    public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
        if (flag) {
            return new FlowExecutionStatus("TRUE");
        } else {
            return new FlowExecutionStatus("FALSE");
        }
    }
}
