package io.csdn.batchdemo.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.converter.DefaultJobParametersConverter;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.SimpleJobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BatchConfig {

    @Value("${spring.batch.chunk.size:5}")
    private int chunkSize;

    private final StepBuilderFactory stepBuilderFactory;

    private final JobBuilderFactory jobBuilderFactory;

    private final JobExplorer jobExplorer;

    private final JobRepository jobRepository;

    private final JobRegistry jobRegistry;

    private final JobLauncher jobLauncher;

    private ApplicationContext applicationContext;

    public BatchConfig(StepBuilderFactory stepBuilderFactory,
                       JobBuilderFactory jobBuilderFactory,
                       JobExplorer jobExplorer,
                       JobRepository jobRepository,
                       JobRegistry jobRegistry,
                       JobLauncher jobLauncher,
                       ApplicationContext applicationContext) {
        this.stepBuilderFactory = stepBuilderFactory;
        this.jobBuilderFactory = jobBuilderFactory;
        this.jobExplorer = jobExplorer;
        this.jobRepository = jobRepository;
        this.jobRegistry = jobRegistry;
        this.jobLauncher = jobLauncher;
        this.applicationContext = applicationContext;
    }

    @Bean public JobRegistryBeanPostProcessor jobRegistry() throws Exception {
        JobRegistryBeanPostProcessor registrar = new JobRegistryBeanPostProcessor();

        registrar.setJobRegistry(this.jobRegistry);
        registrar.setBeanFactory(this.applicationContext.getAutowireCapableBeanFactory());
        registrar.afterPropertiesSet();

        return registrar;
    }

    @Bean public JobOperator jobOperator() throws Exception {
        SimpleJobOperator jobOperator = new SimpleJobOperator();

        jobOperator.setJobLauncher(this.jobLauncher);
        jobOperator.setJobParametersConverter(new DefaultJobParametersConverter());
        jobOperator.setJobRepository(this.jobRepository);
        jobOperator.setJobExplorer(this.jobExplorer);
        jobOperator.setJobRegistry(this.jobRegistry);
        jobOperator.afterPropertiesSet();

        return jobOperator;
    }

    @Bean
    @Qualifier("stopJob")
    public Job anotherJob() {
        return this.jobBuilderFactory.get("stopJob")
                .start(stopJobStep())
                .build();
    }

    @Bean public Step stopJobStep() {
        return this.stepBuilderFactory.get("stopJobStep")
                .tasklet(tasklet(null))
                .build();
    }

    @Bean
    @StepScope
    public Tasklet tasklet(@Value("#{jobParameters['parameter']}") String parameter) {
        return (contribution, chunkContext) -> {
            System.out.println("正在处理数据 " + parameter);
            Thread.sleep(1000);
            return RepeatStatus.CONTINUABLE;
        };
    }
}
