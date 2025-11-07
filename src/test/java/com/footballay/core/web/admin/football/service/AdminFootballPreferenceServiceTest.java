package com.footballay.core.web.admin.football.service;

import com.footballay.core.domain.football.FootballRoot;
import com.footballay.core.domain.football.dto.PlayerDto;
import com.footballay.core.domain.football.preference.FootballPreferenceService;
import com.footballay.core.domain.football.preference.dto.PlayerCustomPhotoDto;
import com.footballay.core.web.admin.football.response.mapper.FootballPreferenceMapper;
import com.footballay.core.web.admin.football.response.PlayerPhotosResponse;
import com.footballay.core.web.admin.football.response.PlayerResponse;
import com.footballay.core.web.common.dto.ApiResponse;
import com.footballay.core.web.common.service.ApiCommonResponseService;
import org.joda.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTest
class AdminFootballPreferenceServiceTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AdminFootballPreferenceServiceTest.class);
    @MockitoBean
    private FootballPreferenceService footballPreferenceService;
    @MockitoBean
    private FootballRoot footballRoot;
    @Autowired
    private ApiCommonResponseService apiResponseService;
    @Autowired
    private AdminFootballPreferenceService adminFootballPreferenceService;
    private final int CODE_SUCCESS = apiResponseService.CODE_SUCCESS;
    private final int CODE_FAILURE = apiResponseService.CODE_FAILURE;

    private PlayerDto createPlayerDto(long id, String name) {
        return new PlayerDto(id, name, "kor_" + name, "https://photourl.com/" + id, "F");
    }

    private PlayerCustomPhotoDto createPlayerCustomPhotoDto(long photoId, long playerId, String photoUrl, boolean active) {
        return new PlayerCustomPhotoDto(photoId, playerId, photoUrl, active, LocalDateTime.now().toString(), LocalDateTime.now().toString());
    }


    @Nested
    @DisplayName("getSquadCustomPhotos 메서드 테스트")
    class GetSquadCustomPhotosTest {
        @Test
        @DisplayName("정상적으로 팀의 선수 커스텀 사진을 가져온다")
        void getSquadCustomPhotos_Success() {
            // Given
            Authentication auth = mock(Authentication.class);
            when(auth.isAuthenticated()).thenReturn(true);
            when(auth.getName()).thenReturn("user1");
            long teamId = 100L;
            String requestUrl = "/api/admin/football/team/100/photos";
            List<PlayerDto> squadOfTeam = Arrays.asList(createPlayerDto(1L, "Ronaldo"), createPlayerDto(2L, "Messi"));
            Map<Long, String> squadActiveCustomPhotos = Map.of(1L, "https://photos.com/1.png", 2L, "https://photos.com/2.png");
            when(footballRoot.getSquadOfTeam(teamId)).thenReturn(squadOfTeam);
            when(footballPreferenceService.getSquadActiveCustomPhotos("user1", teamId)).thenReturn(squadActiveCustomPhotos);
            // When
            ApiResponse<PlayerResponse> response = adminFootballPreferenceService.getSquadCustomPhotos(auth, teamId, requestUrl);
            // Then
            assertThat(response.response()).hasSize(2);
            assertThat(response.metaData().responseCode()).isEqualTo(CODE_SUCCESS);
            verify(footballRoot, times(1)).getSquadOfTeam(teamId);
            verify(footballPreferenceService, times(1)).getSquadActiveCustomPhotos("user1", teamId);
        }

        @Test
        @DisplayName("인증되지 않은 사용자가 접근하면 예외를 반환한다")
        void getSquadCustomPhotos_Unauthenticated() {
            // Given
            Authentication auth = mock(Authentication.class);
            when(auth.isAuthenticated()).thenReturn(false);
            long teamId = 100L;
            String requestUrl = "/api/admin/football/team/100/photos";
            // When
            ApiResponse<PlayerResponse> response = adminFootballPreferenceService.getSquadCustomPhotos(auth, teamId, requestUrl);
            // Then
            assertThat(response.response()).isNullOrEmpty();
            assertThat(response.metaData().responseCode()).isEqualTo(CODE_FAILURE);
            verifyNoInteractions(footballRoot, footballPreferenceService);
        }

        @Test
        @DisplayName("예외 발생 시 실패 응답을 반환한다")
        void getSquadCustomPhotos_Exception() {
            // Given
            Authentication auth = mock(Authentication.class);
            when(auth.isAuthenticated()).thenReturn(true);
            when(auth.getName()).thenReturn("user1");
            long teamId = 100L;
            String requestUrl = "/api/admin/football/team/100/photos";
            when(footballRoot.getSquadOfTeam(teamId)).thenThrow(new RuntimeException("Database error"));
            // When
            ApiResponse<PlayerResponse> response = adminFootballPreferenceService.getSquadCustomPhotos(auth, teamId, requestUrl);
            // Then
            assertThat(response.response()).isNull();
            assertThat(response.metaData().responseCode()).isEqualTo(CODE_FAILURE);
        }
    }


    @Nested
    @DisplayName("getPlayerRegisteredPhotos 메서드 테스트")
    class GetPlayerRegisteredPhotosTest {
        @Test
        @DisplayName("정상적으로 선수의 등록된 이미지를 가져온다")
        void getPlayerRegisteredPhotos_Success() {
            // Given
            Authentication auth = mock(Authentication.class);
            when(auth.isAuthenticated()).thenReturn(true);
            when(auth.getName()).thenReturn("user1");
            long playerId = 1L;
            String requestUrl = "/api/admin/football/player/1/photos";
            PlayerDto playerDto = createPlayerDto(1L, "Ronaldo");
            PlayerCustomPhotoDto photoDto1 = createPlayerCustomPhotoDto(1L, 1L, "https://photos.com/1_1.png", true);
            PlayerCustomPhotoDto photoDto2 = createPlayerCustomPhotoDto(2L, 1L, "https://photos.com/1_2.png", false);
            List<PlayerCustomPhotoDto> allPhotos = Arrays.asList(photoDto1, photoDto2);
            when(footballRoot.getPlayer(playerId)).thenReturn(playerDto);
            when(footballPreferenceService.getAllPhotosOfPlayerIncludeInactive("user1", playerId)).thenReturn(allPhotos);
            // When
            ApiResponse<PlayerPhotosResponse> response = adminFootballPreferenceService.getPlayerRegisteredPhotos(auth, playerId, requestUrl);
            // Then
            assertThat(response.response()).hasSize(1);
            assertThat(response.response()[0]).isEqualTo(FootballPreferenceMapper.toPlayerPhotosResponse(playerDto, allPhotos));
            assertThat(response.metaData().responseCode()).isEqualTo(CODE_SUCCESS);
            verify(footballRoot, times(1)).getPlayer(playerId);
            verify(footballPreferenceService, times(1)).getAllPhotosOfPlayerIncludeInactive("user1", playerId);
        }

        @Test
        @DisplayName("인증되지 않은 사용자가 접근하면 예외를 반환한다")
        void getPlayerRegisteredPhotos_Unauthenticated() {
            // Given
            Authentication auth = mock(Authentication.class);
            when(auth.isAuthenticated()).thenReturn(false);
            long playerId = 1L;
            String requestUrl = "/api/admin/football/player/1/photos";
            // When
            ApiResponse<PlayerPhotosResponse> response = adminFootballPreferenceService.getPlayerRegisteredPhotos(auth, playerId, requestUrl);
            // Then
            assertThat(response.response()).isNull();
            assertThat(response.metaData().responseCode()).isEqualTo(CODE_FAILURE);
            verifyNoInteractions(footballRoot, footballPreferenceService);
        }

        @Test
        @DisplayName("예외 발생 시 실패 응답을 반환한다")
        void getPlayerRegisteredPhotos_Exception() {
            // Given
            Authentication auth = mock(Authentication.class);
            when(auth.isAuthenticated()).thenReturn(true);
            when(auth.getName()).thenReturn("user1");
            long playerId = 1L;
            String requestUrl = "/api/admin/football/player/1/photos";
            when(footballRoot.getPlayer(playerId)).thenThrow(new RuntimeException("Database error"));
            // When
            ApiResponse<PlayerPhotosResponse> response = adminFootballPreferenceService.getPlayerRegisteredPhotos(auth, playerId, requestUrl);
            // Then
            assertThat(response.response()).isNull();
            assertThat(response.metaData().responseCode()).isEqualTo(CODE_FAILURE);
        }
    }


    @Nested
    @DisplayName("uploadPlayerPhoto 메서드 테스트")
    class UploadPlayerPhotoTest {
        @Test
        @DisplayName("정상적으로 선수 이미지를 업로드한다")
        void uploadPlayerPhoto_Success() {
            // Given
            Authentication auth = mock(Authentication.class);
            when(auth.isAuthenticated()).thenReturn(true);
            when(auth.getName()).thenReturn("user1");
            long playerId = 1L;
            MultipartFile photoFile = mock(MultipartFile.class);
            when(photoFile.getContentType()).thenReturn("image/png");
            String requestUrl = "/api/admin/football/player/1/upload";
            // When
            ApiResponse<String> response = adminFootballPreferenceService.uploadPlayerPhoto(auth, playerId, photoFile, requestUrl);
            // Then
            assertThat(response.response()).containsExactly("success");
            assertThat(response.metaData().responseCode()).isEqualTo(CODE_SUCCESS);
            verify(footballPreferenceService, times(1)).savePlayerCustomPhoto("user1", playerId, photoFile);
        }

        @Test
        @DisplayName("인증되지 않은 사용자가 접근하면 예외를 반환한다")
        void uploadPlayerPhoto_Unauthenticated() {
            // Given
            Authentication auth = mock(Authentication.class);
            when(auth.isAuthenticated()).thenReturn(false);
            long playerId = 1L;
            MultipartFile photoFile = mock(MultipartFile.class);
            String requestUrl = "/api/admin/football/player/1/upload";
            // When
            ApiResponse<String> response = adminFootballPreferenceService.uploadPlayerPhoto(auth, playerId, photoFile, requestUrl);
            // Then
            assertThat(response.response()).isNull();
            assertThat(response.metaData().responseCode()).isEqualTo(CODE_FAILURE);
            verifyNoInteractions(footballPreferenceService);
        }

        @Test
        @DisplayName("유효하지 않은 파일 타입일 경우 예외를 반환한다")
        void uploadPlayerPhoto_InvalidFileType() {
            // Given
            Authentication auth = mock(Authentication.class);
            when(auth.isAuthenticated()).thenReturn(true);
            when(auth.getName()).thenReturn("user1");
            long playerId = 1L;
            MultipartFile photoFile = mock(MultipartFile.class);
            when(photoFile.getContentType()).thenReturn("application/pdf"); // 유효하지 않은 타입
            String requestUrl = "/api/admin/football/player/1/upload";
            // When
            ApiResponse<String> response = adminFootballPreferenceService.uploadPlayerPhoto(auth, playerId, photoFile, requestUrl);
            // Then
            assertThat(response.response()).isNull();
            assertThat(response.metaData().responseCode()).isEqualTo(CODE_FAILURE);
            verifyNoInteractions(footballPreferenceService);
        }

        @Test
        @DisplayName("예외 발생 시 실패 응답을 반환한다")
        void uploadPlayerPhoto_Exception() {
            // Given
            Authentication auth = mock(Authentication.class);
            when(auth.isAuthenticated()).thenReturn(true);
            when(auth.getName()).thenReturn("user1");
            long playerId = 1L;
            MultipartFile photoFile = mock(MultipartFile.class);
            when(photoFile.getContentType()).thenReturn("image/png");
            String requestUrl = "/api/admin/football/player/1/upload";
            doThrow(new RuntimeException("Upload failed")).when(footballPreferenceService).savePlayerCustomPhoto("user1", playerId, photoFile);
            // When
            ApiResponse<String> response = adminFootballPreferenceService.uploadPlayerPhoto(auth, playerId, photoFile, requestUrl);
            // Then
            assertThat(response.response()).isNull();
            assertThat(response.metaData().responseCode()).isEqualTo(CODE_FAILURE);
            verify(footballPreferenceService, times(1)).savePlayerCustomPhoto("user1", playerId, photoFile);
        }
    }


    @Nested
    @DisplayName("activatePhoto 및 deactivatePhoto 메서드 테스트")
    class ActivateDeactivatePhotoTest {
        @Test
        @DisplayName("이미지를 정상적으로 활성화한다")
        void activatePhoto_Success() {
            // Given
            Authentication auth = mock(Authentication.class);
            when(auth.isAuthenticated()).thenReturn(true);
            when(auth.getName()).thenReturn("user1");
            long photoId = 10L;
            String requestUrl = "/api/admin/football/player/1/photo/10/activate";
            when(footballPreferenceService.activatePhoto("user1", photoId)).thenReturn(true);
            // When
            ApiResponse<String> response = adminFootballPreferenceService.activatePhoto(auth, photoId, requestUrl);
            // Then
            assertThat(response.response()).containsExactly("success");
            assertThat(response.metaData().responseCode()).isEqualTo(CODE_SUCCESS);
            verify(footballPreferenceService, times(1)).activatePhoto("user1", photoId);
        }

        @Test
        @DisplayName("이미지 활성화 실패 시 실패 응답을 반환한다")
        void activatePhoto_Failure() {
            // Given
            Authentication auth = mock(Authentication.class);
            when(auth.isAuthenticated()).thenReturn(true);
            when(auth.getName()).thenReturn("user1");
            long photoId = 10L;
            String requestUrl = "/api/admin/football/player/1/photo/10/activate";
            when(footballPreferenceService.activatePhoto("user1", photoId)).thenReturn(false);
            // When
            ApiResponse<String> response = adminFootballPreferenceService.activatePhoto(auth, photoId, requestUrl);
            // Then
            assertThat(response.response()).containsExactly("failed");
            assertThat(response.metaData().responseCode()).isEqualTo(CODE_SUCCESS);
            verify(footballPreferenceService, times(1)).activatePhoto("user1", photoId);
        }

        @Test
        @DisplayName("인증되지 않은 사용자가 접근하면 예외를 반환한다")
        void activatePhoto_Unauthenticated() {
            // Given
            Authentication auth = mock(Authentication.class);
            when(auth.isAuthenticated()).thenReturn(false);
            long photoId = 10L;
            String requestUrl = "/api/admin/football/player/1/photo/10/activate";
            // When
            ApiResponse<String> response = adminFootballPreferenceService.activatePhoto(auth, photoId, requestUrl);
            // Then
            assertThat(response.response()).isNull();
            assertThat(response.metaData().responseCode()).isEqualTo(CODE_FAILURE);
            verifyNoInteractions(footballPreferenceService);
        }

        @Test
        @DisplayName("예외 발생 시 실패 응답을 반환한다")
        void activatePhoto_Exception() {
            // Given
            Authentication auth = mock(Authentication.class);
            when(auth.isAuthenticated()).thenReturn(true);
            when(auth.getName()).thenReturn("user1");
            long photoId = 10L;
            String requestUrl = "/api/admin/football/player/1/photo/10/activate";
            when(footballPreferenceService.activatePhoto("user1", photoId)).thenThrow(new RuntimeException("Activation failed"));
            // When
            ApiResponse<String> response = adminFootballPreferenceService.activatePhoto(auth, photoId, requestUrl);
            // Then
            assertThat(response.response()).isNull();
            assertThat(response.metaData().responseCode()).isEqualTo(CODE_FAILURE);
            verify(footballPreferenceService, times(1)).activatePhoto("user1", photoId);
        }

        @Test
        @DisplayName("이미지를 정상적으로 비활성화한다")
        void deactivatePhoto_Success() {
            // Given
            Authentication auth = mock(Authentication.class);
            when(auth.isAuthenticated()).thenReturn(true);
            when(auth.getName()).thenReturn("user1");
            long photoId = 10L;
            String requestUrl = "/api/admin/football/player/1/photo/10/deactivate";
            when(footballPreferenceService.deactivatePhoto("user1", photoId)).thenReturn(true);
            // When
            ApiResponse<String> response = adminFootballPreferenceService.deactivatePhoto(auth, photoId, requestUrl);
            // Then
            assertThat(response.response()).containsExactly("success");
            assertThat(response.metaData().responseCode()).isEqualTo(CODE_SUCCESS);
            verify(footballPreferenceService, times(1)).deactivatePhoto("user1", photoId);
        }

        @Test
        @DisplayName("이미지 비활성화 실패 시 실패 응답을 반환한다")
        void deactivatePhoto_Failure() {
            // Given
            Authentication auth = mock(Authentication.class);
            when(auth.isAuthenticated()).thenReturn(true);
            when(auth.getName()).thenReturn("user1");
            long photoId = 10L;
            String requestUrl = "/api/admin/football/player/1/photo/10/deactivate";
            when(footballPreferenceService.deactivatePhoto("user1", photoId)).thenReturn(false);
            // When
            ApiResponse<String> response = adminFootballPreferenceService.deactivatePhoto(auth, photoId, requestUrl);
            // Then
            assertThat(response.response()).containsExactly("failed");
            assertThat(response.metaData().responseCode()).isEqualTo(CODE_SUCCESS);
            verify(footballPreferenceService, times(1)).deactivatePhoto("user1", photoId);
        }

        @Test
        @DisplayName("인증되지 않은 사용자가 접근하면 예외를 반환한다")
        void deactivatePhoto_Unauthenticated() {
            // Given
            Authentication auth = mock(Authentication.class);
            when(auth.isAuthenticated()).thenReturn(false);
            long photoId = 10L;
            String requestUrl = "/api/admin/football/player/1/photo/10/deactivate";
            // When
            ApiResponse<String> response = adminFootballPreferenceService.deactivatePhoto(auth, photoId, requestUrl);
            // Then
            assertThat(response.response()).isNull();
            assertThat(response.metaData().responseCode()).isEqualTo(CODE_FAILURE);
            verifyNoInteractions(footballPreferenceService);
        }

        @Test
        @DisplayName("예외 발생 시 실패 응답을 반환한다")
        void deactivatePhoto_Exception() {
            // Given
            Authentication auth = mock(Authentication.class);
            when(auth.isAuthenticated()).thenReturn(true);
            when(auth.getName()).thenReturn("user1");
            long photoId = 10L;
            String requestUrl = "/api/admin/football/player/1/photo/10/deactivate";
            when(footballPreferenceService.deactivatePhoto("user1", photoId)).thenThrow(new RuntimeException("Deactivation failed"));
            // When
            ApiResponse<String> response = adminFootballPreferenceService.deactivatePhoto(auth, photoId, requestUrl);
            // Then
            assertThat(response.response()).isNull();
            assertThat(response.metaData().responseCode()).isEqualTo(CODE_FAILURE);
            verify(footballPreferenceService, times(1)).deactivatePhoto("user1", photoId);
        }
    }
}
