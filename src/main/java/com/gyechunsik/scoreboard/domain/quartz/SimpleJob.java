package com.gyechunsik.scoreboard.domain.quartz;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.time.LocalDateTime;

@Slf4j
public class SimpleJob implements Job {
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.info("SimpleJob executed at {}", LocalDateTime.now());
    }
}
