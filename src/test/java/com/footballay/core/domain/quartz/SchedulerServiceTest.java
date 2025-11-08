package com.footballay.core.domain.quartz;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class SchedulerServiceTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SchedulerServiceTest.class);
    @Autowired
    private SchedulerService schedulerService;
    @Autowired
    private Scheduler scheduler;

    @AfterEach
    void initScheduler() throws SchedulerException {
        scheduler.clear();
    }

    @DisplayName("SimpleJob 을 추가하여서 Quartz 에 job 추가시에 exception 이 발생하지 않음을 테스트 합니다")
    @Test
    void beanInjectionTest() throws InterruptedException {
        schedulerService.start();
        Thread.sleep(10000);
    }
}
