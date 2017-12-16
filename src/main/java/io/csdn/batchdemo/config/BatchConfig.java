package io.csdn.batchdemo.config;

import io.csdn.batchdemo.component.CustomerItemReader;
import io.csdn.batchdemo.component.CustomerItemWriter;
import io.csdn.batchdemo.exception.InvalidDataException;
import io.csdn.batchdemo.listener.JobExecutionListener;
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

    private final JobExecutionListener jobExecutionListener;

    public BatchConfig(StepBuilderFactory stepBuilderFactory,
                       JobBuilderFactory jobBuilderFactory,
                       JobExecutionListener jobExecutionListener) {
        this.stepBuilderFactory = stepBuilderFactory;
        this.jobBuilderFactory = jobBuilderFactory;
        this.jobExecutionListener = jobExecutionListener;
    }

    @Bean public Job customerJob() {
        return this.jobBuilderFactory.get("customerJob")
                .start(skipChunkBasedStep())
                .listener(jobExecutionListener)
                .build();
    }

    @Bean public Step skipChunkBasedStep() {
        return this.stepBuilderFactory.get("skipChunkBasedStep")
                .<List<Customer>, List<Customer>>chunk(chunkSize)
                .reader(customerItemReader())
                .writer(customerItemWriter())
                .faultTolerant()
                .retry(InvalidDataException.class)
                .retryLimit(5)
                .noRetry(NullPointerException.class)
                .skip(TimeoutException.class)
                .skip(IOException.class)
                .skipLimit(3)
                .allowStartIfComplete(true) // 此处仅用于演示使用，建议在正式生产环境中删除
                .build();
    }

    @Bean public ItemReader<List<Customer>> customerItemReader() {
        return new CustomerItemReader();
    }

    @Bean public ItemWriter<List<Customer>> customerItemWriter() {
        return new CustomerItemWriter();
    }
}
