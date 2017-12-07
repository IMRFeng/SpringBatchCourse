package io.csdn.batchdemo;

import io.csdn.batchdemo.model.Customer;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.batch.item.validator.ValidatingItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.validation.BindException;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class BatchConfig {

    private StepBuilderFactory stepBuilderFactory;

    private JobBuilderFactory jobBuilderFactory;

    public BatchConfig(StepBuilderFactory stepBuilderFactory,
                       JobBuilderFactory jobBuilderFactory) {
        this.stepBuilderFactory = stepBuilderFactory;
        this.jobBuilderFactory = jobBuilderFactory;
    }

    @Bean public Job itemProcessorJob() throws Exception {
        return this.jobBuilderFactory.get("itemProcessorJob")
                .start(chunkBasedStep())
                .build();
    }

    @Bean public Step chunkBasedStep() throws Exception {
        return this.stepBuilderFactory.get("chunkBasedStep")
                .<Customer, Customer>chunk(5)
                .reader(csvFileItemReader())
                .processor(itemProcessor())
                .writer(list -> list.forEach(System.out::println))
                .allowStartIfComplete(true)
                .build();
    }

    @Bean public ItemReader<Customer> csvFileItemReader() {
        FlatFileItemReader<Customer> reader = new FlatFileItemReader<>();

        reader.setResource(new ClassPathResource("/data/us-500.csv"));
        reader.setLinesToSkip(1);
        reader.setLineMapper(this.createCustomerLineMapper());

        return reader;
    }

    @Bean public ItemProcessor<Customer, Customer> itemProcessor() throws Exception {
        List<ItemProcessor<Customer, Customer>> delegates = new ArrayList<>();

        ValidatingItemProcessor<Customer> customerValidatingItemProcessor = new ValidatingItemProcessor<>(new CustomerValidator());
//        customerValidatingItemProcessor.setFilter(true);

        delegates.add(new UpperCaseItemProcessor());
        delegates.add(customerValidatingItemProcessor);

        CompositeItemProcessor<Customer, Customer> compositeItemProcessor = new CompositeItemProcessor<>();
        compositeItemProcessor.setDelegates(delegates);
        compositeItemProcessor.afterPropertiesSet();

        return compositeItemProcessor;
    }

    private LineMapper<Customer> createCustomerLineMapper() {
        DefaultLineMapper<Customer> customerLineMapper = new DefaultLineMapper<>();

        customerLineMapper.setLineTokenizer(this.createCustomerLineTokenizer());
        customerLineMapper.setFieldSetMapper(this.createCustomerFieldSetMapper());
        customerLineMapper.afterPropertiesSet();

        return customerLineMapper;
    }

    private LineTokenizer createCustomerLineTokenizer() {
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames(new String[] {"first_name", "last_name", "company_name", "address", "city", "country",
                "state", "zip", "phone1", "phone2", "email", "web"});
        return tokenizer;
    }

    private FieldSetMapper<Customer> createCustomerFieldSetMapper() {
        BeanWrapperFieldSetMapper<Customer> customerFieldSetMapper = new BeanWrapperFieldSetMapper<>();
        customerFieldSetMapper.setTargetType(Customer.class);
        return customerFieldSetMapper;
    }

    private class UpperCaseItemProcessor implements ItemProcessor<Customer, Customer> {

        @Override
        public Customer process(Customer item) throws Exception {
            return new Customer(item.getFirstName().toUpperCase(), item.getLastName(), item.getCompanyName(), item.getAddress(),
                    item.getCity(), item.getCountry(), item.getState(), item.getZip(), item.getPhone1(), item.getPhone2(),
                    item.getEmail(), item.getWeb());
        }
    }
}
