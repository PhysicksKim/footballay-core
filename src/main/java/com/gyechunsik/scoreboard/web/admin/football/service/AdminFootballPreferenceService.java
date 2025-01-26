package com.gyechunsik.scoreboard.web.admin.football.service;

import com.gyechunsik.scoreboard.domain.football.FootballRoot;
import com.gyechunsik.scoreboard.domain.football.dto.PlayerDto;
import com.gyechunsik.scoreboard.domain.football.preference.FootballPreferenceService;
import com.gyechunsik.scoreboard.domain.football.preference.dto.PlayerCustomPhotoDto;
import com.gyechunsik.scoreboard.web.admin.football.response.PreferenceKeyResponse;
import com.gyechunsik.scoreboard.web.admin.football.response.mapper.FootballPreferenceMapper;
import com.gyechunsik.scoreboard.web.admin.football.response.PlayerPhotosResponse;
import com.gyechunsik.scoreboard.web.admin.football.response.PlayerResponse;
import com.gyechunsik.scoreboard.web.common.dto.ApiResponse;
import com.gyechunsik.scoreboard.web.common.service.ApiCommonResponseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class AdminFootballPreferenceService {

    private final FootballPreferenceService footballPreferenceService;
    private final ApiCommonResponseService apiResponseService;
    private final FootballRoot footballRoot;

    public ApiResponse<PreferenceKeyResponse> createPreferenceKey(@Nullable Authentication authentication, String requestUrl) {
        try{
            if(authentication==null || !authentication.isAuthenticated()) {
                return apiResponseService.createFailureResponse("Not authenticated", "/api/admin/football/createPreferenceKey");
            }

            String keyHash = footballPreferenceService.createPreferenceKey(authentication.getName());
            PreferenceKeyResponse[] response = {FootballPreferenceMapper.toPreferenceKeyResponse(keyHash)};

            return apiResponseService.createSuccessResponse(response, requestUrl);
        } catch (Exception e) {
            log.error("Failed to create preference key", e);
            return apiResponseService.createFailureResponse("failed to create preference key", requestUrl);
        }
    }

    public ApiResponse<PreferenceKeyResponse> reissuePreferenceKey(@Nullable Authentication authentication, String requestUrl) {
        try {
            if(authentication==null || !authentication.isAuthenticated()) {
                return apiResponseService.createFailureResponse("Not authenticated", "/api/admin/football/reissuePreferenceKey");
            }

            String keyHash = footballPreferenceService.reissuePreferenceKey(authentication.getName());
            PreferenceKeyResponse[] response = {FootballPreferenceMapper.toPreferenceKeyResponse(keyHash)};

            return apiResponseService.createSuccessResponse(response, requestUrl);
        } catch (Exception e) {
            log.error("Failed to reissue preference key", e);
            return apiResponseService.createFailureResponse("failed to reissue preference key", requestUrl);
        }
    }

    public ApiResponse<PlayerResponse> getSquadCustomPhotos(@Nullable Authentication auth, long teamId, String requestUrl) {
        Map<String, String> params = Map.of("teamId", String.valueOf(teamId));
        try {
            if(auth==null || !auth.isAuthenticated()) {
                throw new IllegalArgumentException("Not authenticated Authentication:"+auth);
            }
            String username = auth.getName();
            List<PlayerDto> squadOfTeam = footballRoot.getSquadOfTeam(teamId);
            Map<Long, String> squadActiveCustomPhotos = footballPreferenceService.getSquadActiveCustomPhotos(username, teamId);
            PlayerResponse[] responseArray = createPlayerResponse(squadOfTeam, squadActiveCustomPhotos);
            return apiResponseService.createSuccessResponse(responseArray, requestUrl, params);
        } catch (Exception e) {
            log.error("Failed to get squad custom photos", e);
            return apiResponseService.createFailureResponse("failed to get squad custom photos", requestUrl);
        }
    }

    public ApiResponse<PlayerPhotosResponse> getPlayerRegisteredPhotos(@Nullable Authentication auth, long playerId, String requestUrl) {
        Map<String, String> params = Map.of("playerId", String.valueOf(playerId));
        try {
            if(auth==null || !auth.isAuthenticated()) {
                throw new IllegalArgumentException("Not authenticated Authentication:"+auth);
            }
            String username = auth.getName();
            PlayerDto playerDto = footballRoot.getPlayer(playerId);
            List<PlayerCustomPhotoDto> allPhotos = footballPreferenceService.getAllPhotosOfPlayerIncludeInactive(username, playerId);
            PlayerPhotosResponse[] responseArr = {FootballPreferenceMapper.toPlayerPhotosResponse(playerDto, allPhotos)};
            return apiResponseService.createSuccessResponse(responseArr, requestUrl, params);
        } catch (Exception e) {
            log.error("Failed to get player registered photos", e);
            return apiResponseService.createFailureResponse("failed to get player registered photos", requestUrl, params);
        }
    }

    public ApiResponse<String> uploadPlayerPhoto(@Nullable Authentication auth, long playerId, MultipartFile photoFile, String requestUrl) {
        Map<String, String> params = Map.of("playerId", String.valueOf(playerId));
        try {
            if(auth==null || !auth.isAuthenticated()) {
                throw new IllegalArgumentException("Not authenticated Authentication:"+auth);
            }
            if(photoFile==null || !"image/png".equals(photoFile.getContentType())) {
                throw new IllegalArgumentException("Invalid photo file contentType:"+(photoFile==null ? "null" : photoFile.getContentType()));
            }

            String username = auth.getName();
            footballPreferenceService.savePlayerCustomPhoto(username, playerId, photoFile);
            String[] responseArr = {"success"};
            return apiResponseService.createSuccessResponse(responseArr, requestUrl, params);
        } catch (Exception e) {
            log.error("Failed to upload player photo", e);
            return apiResponseService.createFailureResponse("failed to upload player photo", requestUrl, params);
        }
    }

    public ApiResponse<String> activatePhoto(@Nullable Authentication auth, long photoId, String requestUrl) {
        Map<String, String> params = Map.of("photoId", String.valueOf(photoId));
        try {
            if(auth==null || !auth.isAuthenticated()) {
                throw new IllegalArgumentException("Not authenticated Authentication:"+auth);
            }
            String username = auth.getName();
            boolean success = footballPreferenceService.activatePhoto(username, photoId);
            String[] responseArr = {success ? "success" : "failed"};
            return apiResponseService.createSuccessResponse(responseArr, requestUrl, params);
        } catch (Exception e) {
            log.error("Failed to activate photo", e);
            return apiResponseService.createFailureResponse("failed to activate photo", requestUrl, params);
        }
    }

    public ApiResponse<String> deactivatePhoto(@Nullable Authentication auth, long photoId, String requestUrl) {
        Map<String, String> params = Map.of("photoId", String.valueOf(photoId));
        try {
            if(auth==null || !auth.isAuthenticated()) {
                throw new IllegalArgumentException("Not authenticated Authentication:"+auth);
            }
            String username = auth.getName();
            boolean success = footballPreferenceService.deactivatePhoto(username, photoId);
            String[] responseArr = {success ? "success" : "failed"};
            return apiResponseService.createSuccessResponse(responseArr, requestUrl, params);
        } catch (Exception e) {
            log.error("Failed to deactivate photo", e);
            return apiResponseService.createFailureResponse("failed to deactivate photo", requestUrl, params);
        }
    }

    public ApiResponse<String> useDefaultProfilePhoto(Authentication auth, long playerId, String requestUrl) {
        Map<String, String> params = Map.of("playerId", String.valueOf(playerId));
        try {
            if(auth==null || !auth.isAuthenticated()) {
                throw new IllegalArgumentException("Not authenticated Authentication:"+auth);
            }
            log.info("useDefaultProfilePhoto authentication:{} playerId:{}", auth, playerId);
            boolean success = footballPreferenceService.switchToDefaultPhoto(playerId);
            String[] responseArr = {success ? "success" : "failed"};
            return apiResponseService.createSuccessResponse(responseArr, requestUrl, params);
        } catch (Exception e) {
            log.error("Failed to use default profile image", e);
            return apiResponseService.createFailureResponse("failed to use default profile image", requestUrl, params);
        }
    }

    public ApiResponse<String> deletePhoto(@Nullable Authentication auth, long photoId, String requestUrl) {
        Map<String, String> params = Map.of("photoId", String.valueOf(photoId));
        try {
            if(auth==null || !auth.isAuthenticated()) {
                throw new IllegalArgumentException("Not authenticated Authentication:"+auth);
            }
            String username = auth.getName();
            boolean success = footballPreferenceService.deletePhoto(username, photoId);
            String[] responseArr = {success ? "success" : "failed"};
            return apiResponseService.createSuccessResponse(responseArr, requestUrl, params);
        } catch (Exception e) {
            log.error("Failed to delete photo", e);
            return apiResponseService.createFailureResponse("failed to delete photo", requestUrl, params);
        }
    }

    private PlayerResponse[] createPlayerResponse(List<PlayerDto> squadOfTeam, Map<Long, String> squadActiveCustomPhotos) {
        List<PlayerResponse> responseList = new ArrayList<>();
        for (PlayerDto player : squadOfTeam) {
            String photoUrl = squadActiveCustomPhotos.get(player.id());
            PlayerResponse responseDto = FootballPreferenceMapper.toPlayerDtoWithCustomPhoto(player, photoUrl);
            responseList.add(responseDto);
        }
        return responseList.toArray(new PlayerResponse[]{});
    }

}
