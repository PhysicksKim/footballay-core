package com.gyechunsik.scoreboard.domain.quartz;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.query.sqm.TemporalUnit;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Slf4j
@Service
public class SchedulerService {

    @Autowired
    private Scheduler scheduler;

    @PostConstruct
    public void start() {
        try {
            JobDetail jobDetail = JobBuilder.newJob(SimpleJob.class)
                    .withIdentity("simpleJob", "group1")
                    .build();
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("simpleTrigger", "group1")
                    .startAt(Date.from(Instant.now().plus(1, ChronoUnit.MINUTES)))
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInSeconds(10)
                            .repeatForever())
                    .build();

            scheduler.scheduleJob(jobDetail, trigger);

            log.info("스케쥴러 시작 , 10분 후 SimpleJob 실행 , {} ", LocalDateTime.now().plusMinutes(1));
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }
}