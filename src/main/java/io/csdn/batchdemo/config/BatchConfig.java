package io.csdn.batchdemo.config;

import io.csdn.batchdemo.component.CustomerItemReader;
import io.csdn.batchdemo.component.CustomerItemWriter;
import io.csdn.batchdemo.exception.CustomerSkipException;
import io.csdn.batchdemo.exception.InvalidDataException;
import io.csdn.batchdemo.listener.CustomerChunkListener;
import io.csdn.batchdemo.listener.CustomerSkipListener;
import io.csdn.batchdemo.listener.JobExecutionTimeListener;
import io.csdn.batchdemo.listener.StepCheckingListener;
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

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

@Configuration
public class BatchConfig {

    @Value("${spring.batch.chunk.size:5}")
    private int chunkSize;

    private final StepBuilderFactory stepBuilderFactory;

    private final JobBuilderFactory jobBuilderFactory;

    private final JobExecutionTimeListener jobExecutionTimeListener;

    public BatchConfig(StepBuilderFactory stepBuilderFactory,
                       JobBuilderFactory jobBuilderFactory,
                       JobExecutionTimeListener jobExecutionTimeListener) {
        this.stepBuilderFactory = stepBuilderFactory;
        this.jobBuilderFactory = jobBuilderFactory;
        this.jobExecutionTimeListener = jobExecutionTimeListener;
    }

    @Bean public Job customerJob() {
        return this.jobBuilderFactory.get("customerJob")
                .start(skipChunkBasedStep())
                .listener(jobExecutionTimeListener)
                .build();
    }

    @Bean public Step skipChunkBasedStep() {
        return this.stepBuilderFactory.get("skipChunkBasedStep")
                .listener(new StepCheckingListener())
                .<List<Customer>, List<Customer>>chunk(chunkSize)
                .reader(customerItemReader())
                .writer(customerItemWriter())
                .faultTolerant()
                .retry(CustomerSkipException.class)
                .retryLimit(3)
                .noRetry(NullPointerException.class)
                .skip(CustomerSkipException.class)
                .skipLimit(1)
                .allowStartIfComplete(true) // 此处仅用于演示使用，建议在正式生产环境中删除
                .listener(new CustomerSkipListener())
                .listener(new CustomerChunkListener())
                .build();
    }

    @Bean public ItemReader<List<Customer>> customerItemReader() {
        return new CustomerItemReader();
    }

    @Bean public ItemWriter<List<Customer>> customerItemWriter() {
        return new CustomerItemWriter();
    }
}
