package io.csdn.batchdemo.config;

import io.csdn.batchdemo.component.DataItemProcessor;
import io.csdn.batchdemo.component.DataItemWriter;
import io.csdn.batchdemo.listener.JobExecutionTimeListener;
import io.csdn.batchdemo.listener.StepCheckingListener;
import io.csdn.batchdemo.model.BatchDataSource;
import io.csdn.batchdemo.model.BatchDataDestination;
import io.csdn.batchdemo.repository.BatchDataSourceRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.text.RandomStringGenerator;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.*;
import org.springframework.batch.item.database.*;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.retry.RetryContext;
import org.springframework.retry.backoff.BackOffContext;
import org.springframework.retry.backoff.BackOffInterruptedException;
import org.springframework.retry.backoff.BackOffPolicy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Slf4j
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

    private DataSource dataSource;

    private BatchDataSourceRepository batchDataSourceRepository;

    private long i = 0;
    private RandomStringGenerator generatorAToZ = new RandomStringGenerator.Builder().withinRange('A', 'Z').build();
    private int columnNumber = RandomUtils.nextInt(1, 10);

    public BatchConfig(StepBuilderFactory stepBuilderFactory,
                       JobBuilderFactory jobBuilderFactory,
                       JobExecutionTimeListener jobExecutionTimeListener,
                       BatchDataSourceRepository batchDataSourceRepository) {
        this.stepBuilderFactory = stepBuilderFactory;
        this.jobBuilderFactory = jobBuilderFactory;
        this.jobExecutionTimeListener = jobExecutionTimeListener;
        this.batchDataSourceRepository = batchDataSourceRepository;
    }

    /**
     * 用于mock数据的Job
     * @return
     */
    @Bean public Job mockDataJob() {
        return this.jobBuilderFactory.get("mockDataJob")
                .start(mockDataStep())
                .build();
    }

    /**
     * 用于生成数据的步骤
     * @return
     */
    @Bean public Step mockDataStep() {
        return this.stepBuilderFactory.get("mockDataStep")
                .tasklet((contribution, chunkContext) -> {
                    i++;
                    this.batchDataSourceRepository.save(BatchDataSource.builder()
                            .subClass(generatorAToZ.generate(1))
                            .parentClass(generatorAToZ.generate(1))
                            .dataText("这里是第" + i + "行")
                            .columnNumber(columnNumber).build());
                    if (i == 100000) { //执行10W次然后终止
                        return RepeatStatus.FINISHED;
                    }
                    return RepeatStatus.CONTINUABLE;
                })
                .allowStartIfComplete(true)
                .build();
    }

    @Bean public Job dataPartitioningJob() throws Exception {
        return this.jobBuilderFactory.get("dataPartitioningJob")
                .start(masterStep())
                .listener(jobExecutionTimeListener)
                .build();
    }

    @Bean public Step masterStep() throws Exception {
        return stepBuilderFactory.get("masterStep")
                .partitioner(slaveStep().getName(), partitioner())
                .step(slaveStep())
                .gridSize(30)
                .taskExecutor(taskExecutor())
                .build();
    }

    @Bean public Step slaveStep() throws Exception {
        return this.stepBuilderFactory.get("slaveStep")
                .allowStartIfComplete(true)
                .listener(new StepCheckingListener())
                .listener(new JobExecutionTimeListener())
                .<BatchDataSource, BatchDataDestination>chunk(chunkSize)
                .reader(jdbcPagingItemReader(null, null, null))
                .processor(asyncDataItemProcessor())
                .writer(asyncDataItemWriter())
                .faultTolerant()
                .retry(IllegalArgumentException.class)
                .retryLimit(1000)
                .noRetry(NullPointerException.class)
                .skip(IllegalArgumentException.class)
                .skipLimit(1000)
                .throttleLimit(100)
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

    @Bean public DataRangePartitioner partitioner() {
        DataRangePartitioner dataRangePartitioner = new DataRangePartitioner();

        dataRangePartitioner.setDataSource(this.dataSource);

        return dataRangePartitioner;
    }

    @Bean
    @StepScope
    public ItemStreamReader<BatchDataSource> jdbcPagingItemReader(@Value("#{stepExecutionContext['fromId']}") Integer fromId,
                                                                  @Value("#{stepExecutionContext['toId']}") Integer toId,
                                                                  @Value("#{stepExecutionContext['threadName']}") String threadNames) throws Exception {
        System.out.println("读取 " + fromId + " - " + toId);
        final JdbcPagingItemReader<BatchDataSource> jdbcPagingItemReader = new JdbcPagingItemReader<>();

        jdbcPagingItemReader.setDataSource(this.dataSource);
        jdbcPagingItemReader.setRowMapper(new BeanPropertyRowMapper<BatchDataSource>() {{
            setMappedClass(BatchDataSource.class);
        }});

        jdbcPagingItemReader.setQueryProvider(queryProvider());
        Map<String, Object> parameterValues = new HashMap<>();
        parameterValues.put("fromId", fromId);
        parameterValues.put("toId", toId);
        jdbcPagingItemReader.setParameterValues(parameterValues);

        return jdbcPagingItemReader;
    }

    private PagingQueryProvider queryProvider() {
        SqlPagingQueryProviderFactoryBean provider = new SqlPagingQueryProviderFactoryBean();
        provider.setDataSource(dataSource);
        provider.setSelectClause("SELECT *");
        provider.setFromClause("FROM DATA_SOURCE");
        provider.setWhereClause("WHERE ID >= :fromId AND ID <= :toId");
        provider.setSortKey("ID");
        try {
            return provider.getObject();
        } catch (Exception e) {
            log.error("queryProvider exception {}", e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    @Bean public ItemProcessor<BatchDataSource, BatchDataDestination> dataItemProcessor() {
        return new DataItemProcessor();
    }

    @Bean public AsyncItemProcessor asyncDataItemProcessor() throws Exception {
        AsyncItemProcessor<BatchDataSource, BatchDataDestination> asyncItemProcessor = new AsyncItemProcessor<>();
        asyncItemProcessor.setDelegate(dataItemProcessor());
        asyncItemProcessor.setTaskExecutor(new SimpleAsyncTaskExecutor());
        asyncItemProcessor.afterPropertiesSet();
        return asyncItemProcessor;
    }

    @Bean public ItemWriter<BatchDataDestination> dataItemWriter() {
        return new DataItemWriter();
    }

    @Bean public AsyncItemWriter<BatchDataDestination> asyncDataItemWriter() throws Exception {
        AsyncItemWriter<BatchDataDestination> asyncItemWriter = new AsyncItemWriter<>();
        asyncItemWriter.setDelegate(dataItemWriter());
        asyncItemWriter.afterPropertiesSet();
        return asyncItemWriter;
    }

    @Autowired
    @Qualifier("dataSource")
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
