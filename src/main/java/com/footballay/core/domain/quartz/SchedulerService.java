package com.footballay.core.domain.quartz;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Slf4j
@RequiredArgsConstructor
@Service
public class SchedulerService {

    private final Scheduler scheduler;

    public void start() {
        try {
            JobDetail jobDetail = JobBuilder.newJob(SimpleJob.class)
                    .withIdentity("simpleJob", "group1")
                    .build();
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("simpleTrigger", "group1")
                    .startAt(Date.from(Instant.now().plus(1, ChronoUnit.SECONDS)))
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInSeconds(1)
                            .repeatForever())
                    .build();

            // jobAutowireTestService.test();

            scheduler.scheduleJob(jobDetail, trigger);

            log.info("스케쥴러 시작 , 5초 후 SimpleJob 실행 , 5초 간격으로 실행, 최초 실행 {} ", LocalDateTime.now().plusSeconds(5));
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

}