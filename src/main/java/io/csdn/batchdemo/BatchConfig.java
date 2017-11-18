package io.csdn.batchdemo;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
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
            System.out.println("步骤1");
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
//            stepContribution.setExitStatus(ExitStatus.FAILED);
            System.out.println("步骤3");

            return RepeatStatus.FINISHED;
        }).build();
    }

    @Bean public Job job1() {
        return this.jobBuilderFactory.get("job1")
                .start(step1())
                .next(step2())
                .next(step3())
                .build();
    }

    @Bean public Job transitionJob() {
        return this.jobBuilderFactory.get("transitionJob")
                .start(step1()).on(ExitStatus.COMPLETED.getExitCode()).to(step2())
                .from(step2()).on(ExitStatus.COMPLETED.getExitCode()).to(step3())
                .end().build();
    }
}
