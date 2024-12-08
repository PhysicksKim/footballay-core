package com.gyechunsik.scoreboard.util;

import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

public class TestQuartzJobWaitUtil {

    public static void waitForJobToBeScheduled(Scheduler scheduler, JobKey jobKey) {
        await().atMost(10, TimeUnit.SECONDS).until(() -> {
            try {
                return scheduler.checkExists(jobKey);
            } catch (SchedulerException e) {
                // 예외 처리s
                return false;
            }
        });
    }

    public static void waitForJobToBeRemoved(Scheduler scheduler, JobKey jobKey) {
        await().atMost(10, TimeUnit.SECONDS).until(() -> {
            try {
                return !scheduler.checkExists(jobKey);
            } catch (SchedulerException e) {
                // 예외 처리
                return true;
            }
        });
    }

}
