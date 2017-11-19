package io.csdn.batchdemo;

import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
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
            System.out.println("通过自定义决策者Decider在步骤或流之间进行动态跳转");
            return RepeatStatus.FINISHED;
        }).build();
    }

    @Bean public Step step2() {
        return this.stepBuilderFactory.get("step2").tasklet((stepContribution, chunkContext) -> {
            System.out.println("返回了FALSE");
            return RepeatStatus.FINISHED;
        }).allowStartIfComplete(true).build();
    }

    @Bean public Step step3() {
        return this.stepBuilderFactory.get("step3").tasklet((stepContribution, chunkContext) -> {
            System.out.println("返回了TRUE");
            return RepeatStatus.FINISHED;
        }).build();
    }

    @Bean public JobExecutionDecider getDecider() {
        return new StepBooleanDecider(true);
    }

    @Bean public Job dynamicDecisionStepJob() {
        return this.jobBuilderFactory.get("dynamicDecisionStepJob")
                .start(step1()).on(ExitStatus.COMPLETED.getExitCode()).to(getDecider())
                .from(getDecider()).on("FALSE").to(step2())
                .from(getDecider()).on("TRUE").to(step3())
                .end().build();
    }
}
