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

    private StepBuilderFactory stepBuilderFactory;

    private JobBuilderFactory jobBuilderFactory;

    private JobLauncher jobLauncher;

    private JobRepository jobRepository;

    public BatchConfig(StepBuilderFactory stepBuilderFactory,
                       JobBuilderFactory jobBuilderFactory,
                       JobLauncher jobLauncher,
                       JobRepository jobRepository) {
        this.stepBuilderFactory = stepBuilderFactory;
        this.jobBuilderFactory = jobBuilderFactory;
        this.jobLauncher = jobLauncher;
        this.jobRepository = jobRepository;
    }

    @Bean public Step step1() {
        return this.stepBuilderFactory.get("step1").tasklet((stepContribution, chunkContext) -> {
            System.out.println("内置Job步骤");
            return RepeatStatus.FINISHED;
        }).build();
    }

    @Bean public Step step2() {
        return this.stepBuilderFactory.get("step2").tasklet((stepContribution, chunkContext) -> {
            System.out.println("Job步骤");
            return RepeatStatus.FINISHED;
        }).build();
    }

    @Bean public Step childJobStep() {
        return new JobStepBuilder(new StepBuilder("childJobStep"))
                .job(childJob())
                .launcher(this.jobLauncher)
                .repository(jobRepository)
                .build();
    }

    @Bean public Job childJob() {
        return this.jobBuilderFactory.get("childJob")
                .start(step1())
                .build();
    }

    @Bean public Job parentJob() {
        return this.jobBuilderFactory.get("parentJob")
                .start(childJobStep())
                .next(step2())
                .build();
    }
}
