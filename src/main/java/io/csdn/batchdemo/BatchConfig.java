package io.csdn.batchdemo;

import io.csdn.batchdemo.model.Customer;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.oxm.xstream.XStreamMarshaller;

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

    @Bean public Job xmlFileWriterJob() throws Exception {
        return this.jobBuilderFactory.get("xmlFileWriterJob")
                .start(chunkBasedStep())
                .build();
    }

    @Bean public Step chunkBasedStep() throws Exception {
        return this.stepBuilderFactory.get("chunkBasedStep")
                .<Customer, Customer>chunk(chunkSize)
                .reader(xmlItemReader())
                .writer(xmlItemWriter())
                .allowStartIfComplete(true)
                .build();
    }

    @Bean public ItemReader<Customer> xmlItemReader() {
        StaxEventItemReader<Customer> reader = new StaxEventItemReader<>();

        reader.setResource(new ClassPathResource("/data/us-500.xml"));
        reader.setFragmentRootElementName("customer");
        reader.setUnmarshaller(this.createMarshallerViaXStream());
//        reader.setUnmarshaller(this.createMarshallerViaJaxb());

        return reader;
    }

    @Bean public ItemWriter<Customer> xmlItemWriter() throws Exception {
        StaxEventItemWriter<Customer> writer = new StaxEventItemWriter<>();

        writer.setRootTagName("tagName");
        writer.setMarshaller(this.createMarshallerViaJaxb());
        String path = System.getProperty("user.dir");
        writer.setResource(new FileSystemResource(path.concat("/output/customer.xml")));
        writer.afterPropertiesSet();

        return writer;
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
