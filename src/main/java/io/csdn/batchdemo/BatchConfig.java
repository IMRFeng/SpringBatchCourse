package io.csdn.batchdemo;

import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

@Configuration
public class BatchConfig {

    private StepBuilderFactory stepBuilderFactory;

    private JobBuilderFactory jobBuilderFactory;

    public BatchConfig(StepBuilderFactory stepBuilderFactory, JobBuilderFactory jobBuilderFactory) {
        this.stepBuilderFactory = stepBuilderFactory;
        this.jobBuilderFactory = jobBuilderFactory;
    }

    @Bean public Tasklet tasklet() {
        return (step, chunk) -> {
            System.out.println("步骤名：" + chunk.getStepContext().getStepName()
                    + ", 线程：" + Thread.currentThread().getName());
            return RepeatStatus.FINISHED;
        };
    }

    @Bean public Step step1() {
        return this.stepBuilderFactory.get("step1").tasklet((stepContribution, chunkContext) -> {
            System.out.println("步骤1 - 拆分及并行处理Flow, 步骤名：" + chunkContext.getStepContext().getStepName()
                    + ", 线程：" + Thread.currentThread().getName());
            return RepeatStatus.FINISHED;
        }).build();
    }

    @Bean public Step step2() {
        return this.stepBuilderFactory.get("step2")
                .tasklet(tasklet()).build();
    }

    @Bean public Step step3() {
        return this.stepBuilderFactory.get("step3")
                .tasklet(tasklet()).build();
    }

    @Bean public Step step4() {
        return this.stepBuilderFactory.get("step4")
                .tasklet((stepContribution, chunkContext) -> {
            System.out.println("====");
            return RepeatStatus.FINISHED;
                }).build();
    }

    @Bean public Flow flow1() {
        return new FlowBuilder<Flow>("flow1")
                .start(step2())
                .build();
    }

    @Bean public Flow flow2() {
        return new FlowBuilder<Flow>("flow2")
                .start(step3()).end();
    }

    @Bean public Job splitJob() {
        return this.jobBuilderFactory.get("splitJob")
                .incrementer(new RunIdIncrementer())
                .start(step1())
                .split(new SimpleAsyncTaskExecutor())
                .add(flow2(), flow1())

                .next(step4())
                .end().build();
    }
}
