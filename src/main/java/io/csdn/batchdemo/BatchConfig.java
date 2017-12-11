package io.csdn.batchdemo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.csdn.batchdemo.model.Customer;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.LineAggregator;
import org.springframework.batch.item.file.transform.PassThroughLineAggregator;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.oxm.xstream.XStreamMarshaller;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class BatchConfig {

    @Value("${spring.batch.chunk.size:5}")
    private int chunkSize;

    private StepBuilderFactory stepBuilderFactory;

    private JobBuilderFactory jobBuilderFactory;

    public BatchConfig(StepBuilderFactory stepBuilderFactory,
                       JobBuilderFactory jobBuilderFactory) {
        this.stepBuilderFactory = stepBuilderFactory;
        this.jobBuilderFactory = jobBuilderFactory;
    }

    @Bean public Job fileWriterJob() throws Exception {
        return this.jobBuilderFactory.get("fileWriterJob")
                .start(chunkBasedStep())
                .build();
    }

    @Bean public Step chunkBasedStep() throws Exception {
        return this.stepBuilderFactory.get("chunkBasedStep")
                .<Customer, Customer>chunk(chunkSize)
                .reader(xmlFileItemReader())
                .writer(customerFlatFileItemWriter())
                .allowStartIfComplete(true)
                .build();
    }

    @Bean public ItemReader<Customer> xmlFileItemReader() {
        StaxEventItemReader<Customer> reader = new StaxEventItemReader<>();

        reader.setResource(new ClassPathResource("/data/us-500.xml"));
        reader.setFragmentRootElementName("customer");
        reader.setUnmarshaller(this.createMarshallerViaXStream());

        return reader;
    }

    @Bean public ItemWriter<Customer> customerFlatFileItemWriter() throws Exception {
        FlatFileItemWriter<Customer> fileItemWriter = new FlatFileItemWriter<>();

//        fileItemWriter.setLineAggregator(new PassThroughLineAggregator<>());
        fileItemWriter.setLineAggregator(new CustomerLineAggregator());
        String workingFolder = System.getProperty("user.dir");
        fileItemWriter.setResource(new FileSystemResource(workingFolder.concat("/output/客户输出.json")));
        fileItemWriter.afterPropertiesSet();

        return fileItemWriter;
    }

    private XStreamMarshaller createMarshallerViaXStream() {
        XStreamMarshaller marshaller = new XStreamMarshaller();

        Map<String, Class> aliases = new HashMap<>();
        aliases.put("customer", Customer.class);
        marshaller.setAliases(aliases);

        return marshaller;
    }

    public class CustomerLineAggregator implements LineAggregator<Customer> {

        @Override
        public String aggregate(Customer item) {
            return item.getFirstName() + " " + item.getLastName();
        }
    }
}
