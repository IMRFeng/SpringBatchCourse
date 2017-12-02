package io.csdn.batchdemo;

import io.csdn.batchdemo.reader.DemoInputReader;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class BatchConfig {

    private StepBuilderFactory stepBuilderFactory;

    private JobBuilderFactory jobBuilderFactory;

    public BatchConfig(StepBuilderFactory stepBuilderFactory,
                       JobBuilderFactory jobBuilderFactory) {
        this.stepBuilderFactory = stepBuilderFactory;
        this.jobBuilderFactory = jobBuilderFactory;
    }

    @Bean public DemoInputReader readItems() {
        return new DemoInputReader(Arrays.asList("A", "B", "C", "D"));
    }

    @Bean public Step chunkBasedStep() {
        return this.stepBuilderFactory.get("chunkBasedStep")
                .<String, String>chunk(1)
                .reader(readItems())
                .writer(list -> list.forEach(System.out::println))
//                .allowStartIfComplete(true)
                .build();
    }

    @Bean public Job itemReaderJob() {
        return this.jobBuilderFactory.get("itemReaderJob")
                .start(chunkBasedStep())
                .build();
    }
}
