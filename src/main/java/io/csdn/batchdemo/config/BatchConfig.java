package io.csdn.batchdemo.config;

import io.csdn.batchdemo.component.CustomerItemReader;
import io.csdn.batchdemo.component.CustomerItemWriter;
import io.csdn.batchdemo.exception.InvalidDataException;
import io.csdn.batchdemo.listener.JobExecutionNotificationListener;
import io.csdn.batchdemo.model.Customer;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class BatchConfig {

    @Value("${spring.batch.chunk.size:5}")
    private int chunkSize;

    private final StepBuilderFactory stepBuilderFactory;

    private final JobBuilderFactory jobBuilderFactory;

    private final JobExecutionNotificationListener jobExecutionNotificationListener;

    public BatchConfig(StepBuilderFactory stepBuilderFactory,
                       JobBuilderFactory jobBuilderFactory,
                       JobExecutionNotificationListener executionNotificationListener) {
        this.stepBuilderFactory = stepBuilderFactory;
        this.jobBuilderFactory = jobBuilderFactory;
        this.jobExecutionNotificationListener = executionNotificationListener;
    }

    @Bean public Job customerJob() {
        return this.jobBuilderFactory.get("customerJob")
                .start(retryChunkBasedStep())
                .listener(jobExecutionNotificationListener)
                .build();
    }

    @Bean public Step retryChunkBasedStep() {
        return this.stepBuilderFactory.get("retryChunkBasedStep")
                .<List<Customer>, List<Customer>>chunk(chunkSize)
                .reader(customerItemReader())
                .writer(customerItemWriter())
                .faultTolerant()
                .retry(InvalidDataException.class)
                .retryLimit(5)
                .noRetry(NullPointerException.class)
                .allowStartIfComplete(true)
                .build();
    }

    @Bean public ItemReader<List<Customer>> customerItemReader() {
        return new CustomerItemReader();
    }

    @Bean public ItemWriter<List<Customer>> customerItemWriter() {
        return new CustomerItemWriter();
    }
}
