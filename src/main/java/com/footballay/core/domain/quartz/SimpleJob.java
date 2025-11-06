package com.footballay.core.domain.quartz;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;

import java.time.LocalDateTime;

public class SimpleJob implements Job {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(SimpleJob.class);
    private final JobAutowireTestService jobAutowireTestService;

    public SimpleJob(JobAutowireTestService jobAutowireTestService) {
        this.jobAutowireTestService = jobAutowireTestService;
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.info("SimpleJob executed at {}", LocalDateTime.now());
        jobAutowireTestService.test();
    }
}
