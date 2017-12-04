package io.csdn.batchdemo;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class BatchConfig {

    @Value("${spring.batch.chunk.size:5}")
    private int chunkSize;

    private final StepBuilderFactory stepBuilderFactory;

    private final JobBuilderFactory jobBuilderFactory;

    public BatchConfig(StepBuilderFactory stepBuilderFactory,
                       JobBuilderFactory jobBuilderFactory) {
        this.stepBuilderFactory = stepBuilderFactory;
        this.jobBuilderFactory = jobBuilderFactory;
    }

    @Bean public Job itemProcessorJob() {
        return this.jobBuilderFactory.get("itemProcessorJob")
                .start(chunkBasedStep())
                .build();
    }

    @Bean public Step chunkBasedStep() {
        return this.stepBuilderFactory.get("chunkBasedStep")
                .<String, String>chunk(chunkSize)
                .reader(listItemReader())
                .processor(itemProcessor())
                .writer(items -> items.forEach(System.out::println))
                .allowStartIfComplete(true)
                .build();
    }

    @Bean public ItemReader<String> listItemReader() {
        return new ListItemReader<>(Arrays.asList("a", "b", "c", null, "d", "e", "f"));
    }

    @Bean public ItemProcessor<String, String> itemProcessor() {
        return new UpperCaseItemProcessor();
    }

    private class UpperCaseItemProcessor implements ItemProcessor<String, String> {

        @Override
        public String process(String item) throws Exception {
            if (item.equals("a") || item.equalsIgnoreCase("b")) {
                return null;
            }
            return item.toUpperCase();
        }
    }
}
