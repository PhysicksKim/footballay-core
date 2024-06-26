package com.gyechunsik.scoreboard.domain.quartz;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
public class SimpleJob implements Job {

    private final JobAutowireTestService jobAutowireTestService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.info("SimpleJob executed at {}", LocalDateTime.now());
        jobAutowireTestService.test();
    }
}
