package com.gyechunsik.scoreboard.domain.quartz;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
@ActiveProfiles({"dev","mockapi"})
@SpringBootTest
class SchedulerServiceTest {

    @Autowired
    private SchedulerService schedulerService;

    @DisplayName("")
    @Test
    void beanInjectionTest() throws InterruptedException {
        schedulerService.start();
        Thread.sleep(10000);
    }

}