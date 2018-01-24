package io.csdn.batchdemo.config;

import io.csdn.batchdemo.model.Customer;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
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

    @Bean public Job xmlFileReaderJob() {
        return this.jobBuilderFactory.get("xmlFileReaderJob")
                .start(chunkBasedStep())
                .next(deleteFileStep())
                .build();
    }

    @Bean public Step deleteFileStep() {
        return this.stepBuilderFactory.get("deleteFileStep")
                .tasklet(tasklet(null)).build();
    }

    @Bean public Job anotherJob() {
        return this.jobBuilderFactory.get("anotherJob")
                .start(step1())
                .build();
    }

    @Bean public Step step1() {
        return this.stepBuilderFactory.get("step1")
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("step1 executed");
                    return RepeatStatus.FINISHED;
                })
                .allowStartIfComplete(true)
                .build();
    }

    @Bean @StepScope public Tasklet tasklet(@Value("#{jobParameters['input.file']}") String fileName) {
        return (step, chunk) -> {
            if (deleteLocalFile(fileName)) {
                System.out.println("已成功删除文件" + fileName);
            } else {
                System.out.println("删除文件" + fileName + "失败！");
            }
            return RepeatStatus.FINISHED;
        };
    }

    @Bean public Step chunkBasedStep() {
        return this.stepBuilderFactory.get("chunkBasedStep")
                .<Customer, Customer>chunk(chunkSize)
                .reader(xmlFileItemReader(null))
                .writer(list -> list.forEach(System.out::println))
                .allowStartIfComplete(true)
                .build();
    }

    @Bean @StepScope public ItemStreamReader<Customer> xmlFileItemReader(@Value("#{jobParameters['input.file']}") String fileName) {
        StaxEventItemReader<Customer> reader = new StaxEventItemReader<>();

        reader.setResource(new FileSystemResource(fileName));
        reader.setFragmentRootElementName("customer");
        reader.setUnmarshaller(this.createMarshallerViaXStream());

        return reader;
    }

    private XStreamMarshaller createMarshallerViaXStream() {
        XStreamMarshaller marshaller = new XStreamMarshaller();

        Map<String, Class> aliases = new HashMap<>();
        aliases.put("customer", Customer.class);
        marshaller.setAliases(aliases);

        return marshaller;
    }

    private static boolean deleteLocalFile(String fileName) {
        File file = new File(fileName);
        return file.delete();
    }
}
