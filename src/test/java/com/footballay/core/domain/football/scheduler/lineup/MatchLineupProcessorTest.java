package com.footballay.core.domain.football.scheduler.lineup;

import com.footballay.core.domain.football.external.fetch.ApiCallService;
import com.footballay.core.domain.football.external.fetch.response.FixtureSingleResponse;
import com.footballay.core.domain.football.external.lineup.LineupService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@Slf4j
@ActiveProfiles({"dev","mockapi"})
@SpringBootTest
class MatchLineupProcessorTest {

    @Mock
    private ApiCallService apiCallService;

    @Mock
    private LineupService lineupService;

    @InjectMocks
    private PreviousMatchProcessor previousMatchProcessor;

    @DisplayName("라인업 정보 요청 및 저장 성공")
    @Test
    public void testRequestAndSaveLineupSuccess() throws Exception {
        // Given
        FixtureSingleResponse fixtureSingleResponse = new FixtureSingleResponse();
        when(apiCallService.fixtureSingle(anyLong())).thenReturn(fixtureSingleResponse);
        when(lineupService.existLineupDataInResponse(fixtureSingleResponse)).thenReturn(true);
        when(lineupService.saveLineup(fixtureSingleResponse)).thenReturn(true);

        // When
        boolean result = previousMatchProcessor.requestAndSaveLineup(1L);

        // Then
        assertFalse(result);
        verify(apiCallService, times(1)).fixtureSingle(anyLong());
        log.info("testRequestAndSaveLineupSuccess completed successfully");
    }

    @Test
    public void testRequestAndSaveLineupException() throws Exception {
        // Given
        when(apiCallService.fixtureSingle(anyLong())).thenThrow(new RuntimeException("API call failed"));

        // When
        boolean result = previousMatchProcessor.requestAndSaveLineup(1L);

        // Then
        assertFalse(result);
        verify(apiCallService, times(1)).fixtureSingle(anyLong());
        verify(lineupService, never()).existLineupDataInResponse(any());
        verify(lineupService, never()).saveLineup(any());
        log.info("testRequestAndSaveLineupException completed successfully");
    }
}