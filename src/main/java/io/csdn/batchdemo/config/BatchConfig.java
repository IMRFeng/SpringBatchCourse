package io.csdn.batchdemo.config;

import io.csdn.batchdemo.component.CustomerItemWriter;
import io.csdn.batchdemo.exception.CustomerSkipException;
import io.csdn.batchdemo.listener.JobExecutionTimeListener;
import io.csdn.batchdemo.listener.StepCheckingListener;
import io.csdn.batchdemo.model.Customer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.*;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    public BatchConfig(StepBuilderFactory stepBuilderFactory,
                       JobBuilderFactory jobBuilderFactory,
                       JobExecutionTimeListener jobExecutionTimeListener) {
        this.stepBuilderFactory = stepBuilderFactory;
        this.jobBuilderFactory = jobBuilderFactory;
        this.jobExecutionTimeListener = jobExecutionTimeListener;
    }

    @Bean public Job partitioningJob() throws Exception {
        return this.jobBuilderFactory.get("partitioningJob11")
                .start(masterStep())
                .listener(jobExecutionTimeListener)
                .build();
    }

    @Bean public Step masterStep() throws Exception {
        return stepBuilderFactory.get("masterStep")
                .partitioner(slaveStep().getName(), partitioner())
                .step(slaveStep())
                .gridSize(10)
                .taskExecutor(taskExecutor())
                .build();
    }

    @Bean public Step slaveStep() throws Exception {
        return this.stepBuilderFactory.get("slaveStep")
                .listener(new StepCheckingListener())
                .<Customer, Customer>chunk(chunkSize)
                .reader(jdbcPagingItemReader(null, null, null))
                .writer(customerItemWriter())
                .faultTolerant()
                .retry(CustomerSkipException.class)
                .retryLimit(3)
                .noRetry(NullPointerException.class)
                .skip(CustomerSkipException.class)
                .skipLimit(1)
                .allowStartIfComplete(true)
//                .listener(new CustomerSkipListener())
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
    public ItemReader<Customer> jdbcPagingItemReader(@Value("#{stepExecutionContext['fromId']}") Integer fromId,
                                           @Value("#{stepExecutionContext['toId']}") Integer toId,
                                           @Value("#{stepExecutionContext['threadName']}") String threadName) throws Exception {
        System.out.println("读取 " + fromId + " - " + toId);
        final JdbcPagingItemReader<Customer> jdbcPagingItemReader = new JdbcPagingItemReader<>();

        jdbcPagingItemReader.setDataSource(this.dataSource);
        jdbcPagingItemReader.setRowMapper(new BeanPropertyRowMapper<Customer>() {{
            setMappedClass(Customer.class);
        }});

        jdbcPagingItemReader.setQueryProvider(queryProvider());
        Map<String, Object> parameterValues = new HashMap<>();
        parameterValues.put("fromId", fromId);
        parameterValues.put("toId", toId);
        jdbcPagingItemReader.setParameterValues(parameterValues);

        return jdbcPagingItemReader;
    }

    private MySqlPagingQueryProvider mySqlPagingQueryProvider() {
        MySqlPagingQueryProvider mySqlPagingQueryProvider = new MySqlPagingQueryProvider();
        mySqlPagingQueryProvider.setSelectClause("*");
        mySqlPagingQueryProvider.setFromClause("FROM CUSTOMER");
        mySqlPagingQueryProvider.setWhereClause("WHERE ID >= :fromId AND ID <= :toId");
        Map<String, Order> sortKeys = new HashMap<>(1);
        sortKeys.put("id", Order.ASCENDING);
        mySqlPagingQueryProvider.setSortKeys(sortKeys);

        return mySqlPagingQueryProvider;
    }

    private PagingQueryProvider queryProvider() {
        SqlPagingQueryProviderFactoryBean provider = new SqlPagingQueryProviderFactoryBean();
        provider.setDataSource(dataSource);
        provider.setSelectClause("SELECT *");
        provider.setFromClause("FROM CUSTOMER_A");
        provider.setWhereClause("WHERE ID >= :fromId AND ID <= :toId");
        provider.setSortKey("id");
        try {
            return provider.getObject();
        } catch (Exception e) {
            log.error("queryProvider exception ");
            e.printStackTrace();
        }

        return null;
    }

    @Bean
    @StepScope
    public ItemReader<Customer> hibernateCustomerItemReader(
            @Value("#{stepExecutionContext[fromId]}") final Integer fromId,
            @Value("#{stepExecutionContext[toId]}") final Integer toId,
            @Value("#{stepExecutionContext[threadName]}") final String threadName) throws Exception {
        System.out.println("通过HIBERNATE读取 " + fromId + " - " + toId);
        HibernatePagingItemReader<Customer> hibernateReader = new HibernatePagingItemReader<>();
        hibernateReader.setQueryString("FROM Customer c WHERE c.ID >= :fromId AND c.ID <= :toId ORDER BY c.ID ASC");
        hibernateReader.setSessionFactory(sessionFactory().getObject());
        hibernateReader.setUseStatelessSession(false);
        hibernateReader.setSaveState(false);

        Map<String, Object> parameterValues = new HashMap<>();
        parameterValues.put("fromId", fromId);
        parameterValues.put("toId", toId);
        hibernateReader.setParameterValues(parameterValues);

        hibernateReader.afterPropertiesSet();
        return hibernateReader;
    }

    /**
     * 自定义一个LocalSessionFactoryBean作为session factory为hibernateCustomerItemReader服务,
     * 配置data source, 需要扫描的entities/models的包路径
     * @return LocalSessionFactoryBean
     * @throws IOException
     */
    @Bean public LocalSessionFactoryBean sessionFactory() throws IOException{
        LocalSessionFactoryBean factoryBean = new LocalSessionFactoryBean();
        factoryBean.setDataSource(this.dataSource);
        factoryBean.setPackagesToScan("io.csdn.batchdemo.model");
        factoryBean.afterPropertiesSet();
        return factoryBean;
    }

    /**
     * 自定义一个transaction manager为hibernateCustomerItemReader服务
     * @return JpaTransactionManager
     */
    @Bean public PlatformTransactionManager transactionManager() {
        return new JpaTransactionManager();
    }

    @Bean public ItemWriter<Customer> customerItemWriter() {
        return new CustomerItemWriter();
    }

    @Autowired public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private static class CustomerRowMapper implements RowMapper<Customer> {
        @Override
        public Customer mapRow(ResultSet resultSet, int i) throws SQLException {
            return new Customer(resultSet.getInt("id"), resultSet.getString("first_name"),
                    resultSet.getString("last_name"), resultSet.getString("company_name"),
                    resultSet.getString("address"), resultSet.getString("city"),
                    resultSet.getString("country"), resultSet.getString("state"),
                    resultSet.getString("zip"), resultSet.getString("phone1"),
                    resultSet.getString("phone2"), resultSet.getString("email"), resultSet.getString("web"));
        }
    }
}
