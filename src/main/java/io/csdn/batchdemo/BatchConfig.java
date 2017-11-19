package io.csdn.batchdemo;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BatchConfig {

    private StepBuilderFactory stepBuilderFactory;

    private JobBuilderFactory jobBuilderFactory;

    public BatchConfig(StepBuilderFactory stepBuilderFactory,
                       JobBuilderFactory jobBuilderFactory) {
        this.stepBuilderFactory = stepBuilderFactory;
        this.jobBuilderFactory = jobBuilderFactory;
    }

    @Bean
    @StepScope
    public Tasklet tasklet(@Value("#{jobParameters['parameter']}") String parameter) {
        return (stepContribution, chunkContext) -> {
            System.out.println("接收到的参数为：" + parameter);
            return RepeatStatus.FINISHED;
        };
    }

    @Bean public Step step1() {
        return this.stepBuilderFactory.get("step1")
                .tasklet(tasklet(null)).build();
    }

    @Bean public Job jobParametersJob() {
        return this.jobBuilderFactory.get("jobParametersJob")
                .start(step1())
                .build();
    }
}
