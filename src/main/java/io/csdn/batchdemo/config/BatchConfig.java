package io.csdn.batchdemo.config;

import io.csdn.batchdemo.component.CustomerItemProcessor;
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
import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

@Configuration
public class BatchConfig {

    @Value("${spring.batch.chunk.size:5}")
    private int chunkSize;

    @Value("${thread.pool.core.pool.size}")
    private int corePoolSize;

    @Value("${thread.pool.max.pool.size}")
    private int maxPoolSize;

    @Value("${thread.pool.queue.capacity}")
    private int queueCapacity;

    @Value("${thread.pool.keep.alive.seconds}")
    private int keepAliveSeconds;

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

    @Bean public Job customerJob() throws Exception {
        return this.jobBuilderFactory.get("customerJob")
                .start(asyncChunkBasedStep())
                .listener(jobExecutionTimeListener)
                .build();
    }

    @Bean public Step asyncChunkBasedStep() throws Exception {
        return this.stepBuilderFactory.get("asyncChunkBasedStep")
                .allowStartIfComplete(true)// 此处仅用于演示使用，建议在正式生产环境中删除
                .listener(new StepCheckingListener())
                .<List<Customer>, List<Customer>>chunk(chunkSize)
                .reader(customerItemReader())
                .processor(asyncCustomerProcessor())
                .writer(asyncCustomerWriter())
                .faultTolerant()
                .retry(CustomerSkipException.class)
                .retryLimit(3)
                .noRetry(NullPointerException.class)
                .skip(CustomerSkipException.class)
                .skipLimit(1)
                .listener(new CustomerSkipListener())
//                .listener(new CustomerChunkListener())
//                .taskExecutor(taskExecutor())
                .build();
    }

    @Bean public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(corePoolSize);
        threadPoolTaskExecutor.setMaxPoolSize(maxPoolSize);
        threadPoolTaskExecutor.setQueueCapacity(queueCapacity);
        threadPoolTaskExecutor.setKeepAliveSeconds(keepAliveSeconds);
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;
    }

    @Bean public ItemReader<List<Customer>> customerItemReader() {
        return new CustomerItemReader();
    }

    @Bean public ItemProcessor<List<Customer>, List<Customer>> itemProcessor() {
        return new CustomerItemProcessor();
    }

    @Bean public ItemWriter<List<Customer>> customerItemWriter() {
        return new CustomerItemWriter();
    }

    @Bean public AsyncItemProcessor asyncCustomerProcessor() throws Exception {
        AsyncItemProcessor<List<Customer>, List<Customer>> asyncItemProcessor = new AsyncItemProcessor<>();
        asyncItemProcessor.setDelegate(itemProcessor());
        asyncItemProcessor.setTaskExecutor(taskExecutor());
        asyncItemProcessor.afterPropertiesSet();
        return asyncItemProcessor;
    }

    @Bean public AsyncItemWriter asyncCustomerWriter() throws Exception {
        AsyncItemWriter<List<Customer>> asyncItemWriter = new AsyncItemWriter<>();
        asyncItemWriter.setDelegate(customerItemWriter());
        asyncItemWriter.afterPropertiesSet();
        return asyncItemWriter;
    }
}
