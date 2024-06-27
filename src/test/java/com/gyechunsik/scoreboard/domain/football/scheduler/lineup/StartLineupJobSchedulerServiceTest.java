package com.gyechunsik.scoreboard.domain.football.scheduler.lineup;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.quartz.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Slf4j
@ActiveProfiles({"dev","mockapi"})
@SpringBootTest
class StartLineupJobSchedulerServiceTest {

    @Mock
    private Scheduler scheduler;

    @InjectMocks
    private StartLineupJobSchedulerService startLineupJobSchedulerService;

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
        startLineupJobSchedulerService.addJob(fixtureId, lineupAnnounceTime);

        // Assert
        verify(scheduler, times(1)).scheduleJob(any(JobDetail.class), any(Trigger.class));
    }

    @Test
    public void testAddJobFailure() throws SchedulerException {
        // Arrange
        doThrow(new SchedulerException("Scheduler error")).when(scheduler).scheduleJob(any(JobDetail.class), any(Trigger.class));

        // Act & Assert
        assertThrows(SchedulerException.class, () -> {
            startLineupJobSchedulerService.addJob(fixtureId, lineupAnnounceTime);
        });

        verify(scheduler, times(1)).scheduleJob(any(JobDetail.class), any(Trigger.class));
    }

    @Test
    public void testRemoveJobSuccess() throws SchedulerException {
        // Arrange
        doReturn(true).when(scheduler).deleteJob(any(JobKey.class));

        // Act
        startLineupJobSchedulerService.removeJob(fixtureId);

        // Assert
        verify(scheduler, times(1)).deleteJob(any(JobKey.class));
    }

    @Test
    public void testRemoveJobFailure() throws SchedulerException {
        // Arrange
        doThrow(new SchedulerException("Scheduler error")).when(scheduler).deleteJob(any(JobKey.class));

        // Act & Assert
        assertThrows(SchedulerException.class, () -> {
            startLineupJobSchedulerService.removeJob(fixtureId);
        });

        verify(scheduler, times(1)).deleteJob(any(JobKey.class));
    }

}