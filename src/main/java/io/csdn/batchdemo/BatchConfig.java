package io.csdn.batchdemo;

import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.JobStepBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BatchConfig {

    private final StepBuilderFactory stepBuilderFactory;

    private final JobBuilderFactory jobBuilderFactory;

    private final JobRepository jobRepository;

    public BatchConfig(StepBuilderFactory stepBuilderFactory,
                       JobBuilderFactory jobBuilderFactory,
                       JobRepository jobRepository) {
        this.stepBuilderFactory = stepBuilderFactory;
        this.jobBuilderFactory = jobBuilderFactory;
        this.jobRepository = jobRepository;
    }

    @Bean public Step nestedStep() {
        return this.stepBuilderFactory.get("step1").tasklet((stepContribution, chunkContext) -> {
            System.out.println("嵌套Job步骤");
            return RepeatStatus.FINISHED;
        }).build();
    }

    @Bean public Step parentStep() {
        return this.stepBuilderFactory.get("step2").tasklet((stepContribution, chunkContext) -> {
            System.out.println("父Job步骤");
            return RepeatStatus.FINISHED;
        }).build();
    }

    @Bean public Step childJobStep() {
        return new JobStepBuilder(new StepBuilder("childJobStep"))
                .job(childJob())
                .repository(jobRepository)
                .build();
    }

    @Bean public Job childJob() {
        return this.jobBuilderFactory.get("childJob")
                .start(nestedStep())
                .build();
    }

    @Bean public Job parentJob() {
        return this.jobBuilderFactory.get("parentJob")
                .start(parentStep())
                .next(childJobStep())
                .build();
    }
}
