package com.gyechunsik.scoreboard.domain.quartz;

import org.junit.jupiter.api.Test;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.shaded.org.checkerframework.checker.units.qual.A;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles({"mockapi","dev"})
@SpringBootTest
class SchedulerServiceTest {

    @Autowired
    private Scheduler scheduler;

    @Test
    public void testScheduler() throws Exception {
        Thread.sleep(30000); // 30초 동안 대기하며 SimpleJob의 실행을 확인합니다.
    }
}