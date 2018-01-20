package io.csdn.batchdemo.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.integration.launch.JobLaunchRequest;
import org.springframework.batch.integration.launch.JobLaunchingGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.file.FileWritingMessageHandler;
import org.springframework.integration.file.filters.AcceptOnceFileListFilter;
import org.springframework.integration.file.support.FileExistsMode;
import org.springframework.integration.ftp.filters.FtpSimplePatternFileListFilter;
import org.springframework.integration.ftp.inbound.FtpInboundFileSynchronizer;
import org.springframework.integration.ftp.inbound.FtpInboundFileSynchronizingMessageSource;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;
import org.springframework.messaging.MessageHandler;

import java.io.File;

/**
 * @author Zhantao Feng.
 */
@Configuration
@EnableIntegration
public class FtpIntegrationConfig {

    @Autowired
    @Qualifier("xmlFileReaderJob")
    private Job batchDemoJob;

    @Autowired
    private JobLauncher jobLauncher;

    public String OUTPUT_DIR = "output";

    @Value("${ftp.host}")
    private String ftpHost;

    @Value("${ftp.userName}")
    private String userName;

    @Value("${ftp.password}")
    private String password;

    @Bean public DefaultFtpSessionFactory ftpSessionFactory() {
        final DefaultFtpSessionFactory factory = new DefaultFtpSessionFactory();
        factory.setHost(this.ftpHost);
        factory.setUsername(this.userName);
        factory.setPassword(this.password);
        return factory;
    }

    @Bean public FtpInboundFileSynchronizer ftpInboundFileSynchronizer() {
        final FtpInboundFileSynchronizer fileSynchronizer = new FtpInboundFileSynchronizer(ftpSessionFactory());
        fileSynchronizer.setDeleteRemoteFiles(true);
        fileSynchronizer.setFilter(new FtpSimplePatternFileListFilter("*.xml"));
        return fileSynchronizer;
    }

    @Bean
    @InboundChannelAdapter(channel = "ftpChannel", poller = @Poller(fixedDelay = "1000"))
    public MessageSource<File> ftpMessageSource() {
        FtpInboundFileSynchronizingMessageSource source =
                new FtpInboundFileSynchronizingMessageSource(ftpInboundFileSynchronizer());
        source.setLocalDirectory(new File(OUTPUT_DIR));
        source.setAutoCreateLocalDirectory(true);
        source.setLocalFilter(new AcceptOnceFileListFilter<>());
        return source;
    }

    @Bean
    @ServiceActivator(inputChannel = "ftpChannel")
    public MessageHandler handler() {
        final FileWritingMessageHandler handler = new FileWritingMessageHandler(new File(OUTPUT_DIR));
        handler.setFileExistsMode(FileExistsMode.IGNORE);
        handler.setExpectReply(true);
        handler.setOutputChannelName("parse-file-channel");
        return handler;
    }

    @ServiceActivator(inputChannel = "parse-file-channel", outputChannel = "job-launch-channel")
    public JobLaunchRequest adapt(final File file) throws Exception {
        final JobParameters jobParameters = new JobParametersBuilder()
                .addString("input.file", file.getAbsolutePath())
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();
        return new JobLaunchRequest(this.batchDemoJob, jobParameters);
    }

    @Bean
    @ServiceActivator(inputChannel = "job-launch-channel")
    public JobLaunchingGateway jobHandler() {
        JobLaunchingGateway jobLaunchingGateway = new JobLaunchingGateway(jobLauncher);
        jobLaunchingGateway.setOutputChannelName("finish");
        return jobLaunchingGateway;
    }

    @ServiceActivator(inputChannel = "finish")
    public void finish() {
        System.out.println("FINISH");
    }

}
