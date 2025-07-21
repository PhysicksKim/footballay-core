package com.footballay.core.domain.quartz;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import java.time.LocalDateTime;

public class SimpleJob implements Job {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SimpleJob.class);
    private final JobAutowireTestService jobAutowireTestService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.info("SimpleJob executed at {}", LocalDateTime.now());
        jobAutowireTestService.test();
    }

    public SimpleJob(final JobAutowireTestService jobAutowireTestService) {
        this.jobAutowireTestService = jobAutowireTestService;
    }
}
