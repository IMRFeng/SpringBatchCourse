package io.csdn.batchdemo;

import io.csdn.batchdemo.model.Customer;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
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

    @Bean public Job xmlFileReaderJob() {
        return this.jobBuilderFactory.get("xmlFileReaderJob")
                .start(chunkBasedStep())
                .build();
    }

    @Bean public Step chunkBasedStep() {
        return this.stepBuilderFactory.get("chunkBasedStep")
                .<Customer, Customer>chunk(chunkSize)
                .reader(csvFileItemReader())
                .writer(list -> list.forEach(System.out::println))
                .allowStartIfComplete(true)
                .build();
    }

    @Bean public ItemReader<Customer> csvFileItemReader() {
        StaxEventItemReader<Customer> reader = new StaxEventItemReader<>();

        reader.setResource(new ClassPathResource("/data/us-500.xml"));
        reader.setFragmentRootElementName("customer");
        reader.setUnmarshaller(this.createMarshallerViaXStream());
//        reader.setUnmarshaller(this.createMarshallerViaJaxb());

        return reader;
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
