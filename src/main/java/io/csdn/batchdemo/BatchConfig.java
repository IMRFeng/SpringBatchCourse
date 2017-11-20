package io.csdn.batchdemo;

import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BatchConfig {

    private StepBuilderFactory stepBuilderFactory;

    private JobBuilderFactory jobBuilderFactory;

    public BatchConfig(StepBuilderFactory stepBuilderFactory, JobBuilderFactory jobBuilderFactory) {
        this.stepBuilderFactory = stepBuilderFactory;
        this.jobBuilderFactory = jobBuilderFactory;
    }

    @Bean public Step step1() {
        return this.stepBuilderFactory.get("step1").tasklet((stepContribution, chunkContext) -> {
            System.out.println("步骤1 - 自定义流Flow");
            return RepeatStatus.FINISHED;
        }).build();
    }

    @Bean public Step step2() {
        return this.stepBuilderFactory.get("step2").tasklet((stepContribution, chunkContext) -> {
            System.out.println("步骤2");
            return RepeatStatus.FINISHED;
        }).build();
    }

    @Bean public Step step3() {
        return this.stepBuilderFactory.get("step3").tasklet((stepContribution, chunkContext) -> {
            System.out.println("步骤3");
            return RepeatStatus.FINISHED;
        }).build();
    }

    @Bean public CustomDecider customDecider() {
        return new CustomDecider(false);
    }

    @Bean public Flow flow1() {
        return new FlowBuilder<Flow>("flow1")
                .start(step1())
                .on(ExitStatus.COMPLETED.getExitCode()).to(customDecider())
                .from(customDecider()).on("TRUE").to(step2())
                .build();
    }

    @Bean public Job flow1Job() {
        return this.jobBuilderFactory.get("flow1Job")
                .incrementer(new RunIdIncrementer())
                .start(flow1())
                .next(step3())
                .end().build();
    }

    @Bean public Job flow2Job() {
        return this.jobBuilderFactory.get("flow2Job")
                .incrementer(new RunIdIncrementer())
                .start(step3())
                .on(ExitStatus.COMPLETED.getExitCode()).to(flow1()) //这里无法使用.next执行flow
                .end().build();
    }

    private static class CustomDecider implements JobExecutionDecider {

        private boolean flag;

        public CustomDecider(boolean flag) {
            this.flag = flag;
        }

        @Override
        public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
            if (this.flag) {
                return new FlowExecutionStatus("TRUE");
            }
            return new FlowExecutionStatus("FALSE");
        }
    }
}
