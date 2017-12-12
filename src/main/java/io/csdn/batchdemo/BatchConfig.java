package io.csdn.batchdemo;

import io.csdn.batchdemo.model.Customer;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.oxm.xstream.XStreamMarshaller;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class BatchConfig {

    @Value("${spring.batch.chunk.size:5}")
    private int chunkSize;

    private final StepBuilderFactory stepBuilderFactory;

    private final JobBuilderFactory jobBuilderFactory;

    private final DataSource dataSource;

    private final EntityManagerFactory entityManagerFactory;

    public BatchConfig(StepBuilderFactory stepBuilderFactory,
                       JobBuilderFactory jobBuilderFactory,
                       DataSource dataSource,
                       EntityManagerFactory entityManagerFactory) {
        this.stepBuilderFactory = stepBuilderFactory;
        this.jobBuilderFactory = jobBuilderFactory;
        this.dataSource = dataSource;
        this.entityManagerFactory = entityManagerFactory;
    }

    @Bean public Job databaseWriterJob() throws Exception {
        return this.jobBuilderFactory.get("databaseWriterJob")
                .start(chunkBasedStep())
                .build();
    }

    @Bean public Step chunkBasedStep() throws Exception {
        return this.stepBuilderFactory.get("chunkBasedStep")
                .<Customer, Customer>chunk(chunkSize)
                .reader(xmlItemReader())
                .writer(jpaItemWriter())
                .allowStartIfComplete(true)
                .build();
    }

    @Bean public ItemReader<Customer> xmlItemReader() {
        StaxEventItemReader<Customer> reader = new StaxEventItemReader<>();

        reader.setResource(new ClassPathResource("/data/us-500.xml"));
        reader.setFragmentRootElementName("customer");
        reader.setUnmarshaller(this.createMarshallerViaXStream());

        return reader;
    }

    @Bean public ItemWriter<Customer> jdbcItemWriter() {
        JdbcBatchItemWriter<Customer> itemWriter = new JdbcBatchItemWriter<>();

        itemWriter.setDataSource(this.dataSource);
        itemWriter.setSql("INSERT INTO CUSTOMER(first_name, last_name, company_name, address, city, country, state, zip, " +
                "phone1, phone2, email, web) VALUES(:firstName, :lastName, " +
                ":companyName, :address, :city, :country, :state, :zip, :phone1, :phone2, :email, :web)");
        itemWriter.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        itemWriter.afterPropertiesSet();

        return itemWriter;
    }

    @Bean public ItemWriter<Customer> jpaItemWriter() throws Exception {
        JpaItemWriter<Customer> itemWriter = new JpaItemWriter<>();

        itemWriter.setEntityManagerFactory(this.entityManagerFactory);
        itemWriter.afterPropertiesSet();

        return itemWriter;
    }

    /**
     * JAXB 允许Java开发人员将Java类映射为XML表示方式（Java Architecture for XML Binding）
     * @return
     */
    private Jaxb2Marshaller createMarshallerViaJaxb() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(Customer.class);
        return marshaller;
    }

    private XStreamMarshaller createMarshallerViaXStream() {
        XStreamMarshaller marshaller = new XStreamMarshaller();

        Map<String, Class> aliases = new HashMap<>();
        aliases.put("customer", Customer.class);
        marshaller.setAliases(aliases);

        return marshaller;
    }
}
