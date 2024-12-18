package com.gyechunsik.scoreboard.domain.football.scheduler.lineup;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.quartz.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Slf4j
@ActiveProfiles({"dev","mockapi"})
@SpringBootTest
class MatchLineupJobSchedulerServiceTest {

    @Mock
    private Scheduler scheduler;

    @InjectMocks
    private PreviousMatchJobSchedulerService previousMatchJobSchedulerService;

    private Long fixtureId;
    private ZonedDateTime lineupAnnounceTime;

    @BeforeEach
    public void setUp() {
        fixtureId = 1L;
        lineupAnnounceTime = ZonedDateTime.now().plusMinutes(1);
    }

    @Test
    public void testAddJobSuccess() throws SchedulerException {
        // Arrange
        doReturn(null).when(scheduler).scheduleJob(any(JobDetail.class), any(Trigger.class));

        // Act
        previousMatchJobSchedulerService.addJob(fixtureId, lineupAnnounceTime);

        // Assert
        verify(scheduler, times(1)).scheduleJob(any(JobDetail.class), any(Trigger.class));
    }

    @Test
    public void testAddJobFailure() throws SchedulerException {
        // Arrange
        doThrow(new SchedulerException("Scheduler error")).when(scheduler).scheduleJob(any(JobDetail.class), any(Trigger.class));

        // Act & Assert
        assertThrows(SchedulerException.class, () -> {
            previousMatchJobSchedulerService.addJob(fixtureId, lineupAnnounceTime);
        });

        verify(scheduler, times(1)).scheduleJob(any(JobDetail.class), any(Trigger.class));
    }

    @Test
    public void testRemoveJobSuccess() throws SchedulerException {
        // Arrange
        doReturn(true).when(scheduler).deleteJob(any(JobKey.class));

        // Act
        previousMatchJobSchedulerService.removeJob(fixtureId);

        // Assert
        verify(scheduler, times(1)).deleteJob(any(JobKey.class));
    }

    @Test
    public void testRemoveJobFailure() throws SchedulerException {
        // Arrange
        doThrow(new SchedulerException("Scheduler error")).when(scheduler).deleteJob(any(JobKey.class));

        // Act & Assert
        assertThrows(SchedulerException.class, () -> {
            previousMatchJobSchedulerService.removeJob(fixtureId);
        });

        verify(scheduler, times(1)).deleteJob(any(JobKey.class));
    }

}