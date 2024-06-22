package com.gyechunsik.scoreboard.domain.football.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Component
public class StartLineupJob implements Job {

    private final StartLineupService startLineupService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.info("StartLineupJob executed at {}", LocalDateTime.now());
        startLineupService.getStartLineup();
    }
}
