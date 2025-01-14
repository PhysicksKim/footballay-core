package com.gyechunsik.scoreboard.web.admin.football.service;

import com.gyechunsik.scoreboard.domain.football.FootballRoot;
import com.gyechunsik.scoreboard.domain.football.dto.PlayerDto;
import com.gyechunsik.scoreboard.domain.football.preference.FootballPreferenceService;
import com.gyechunsik.scoreboard.domain.football.preference.dto.PlayerCustomPhotoDto;
import com.gyechunsik.scoreboard.web.admin.football.response.mapper.FootballCustomPhotoMapper;
import com.gyechunsik.scoreboard.web.admin.football.response.mapper.FootballDtoMapper;
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

    private PlayerResponse[] createPlayerResponse(List<PlayerDto> squadOfTeam, Map<Long, String> squadActiveCustomPhotos) {
        List<PlayerResponse> responseList = new ArrayList<>();
        for (PlayerDto player : squadOfTeam) {
            String photoUrl = squadActiveCustomPhotos.get(player.id());
            PlayerResponse responseDto = FootballCustomPhotoMapper.toPlayerDtoWithCustomPhoto(player, photoUrl);
            responseList.add(responseDto);
        }
        return responseList.toArray(new PlayerResponse[]{});
    }

    public ApiResponse<PlayerPhotosResponse> getPlayerRegisteredImages(Authentication auth, long playerId, String requestUrl) {
        Map<String, String> params = Map.of("playerId", String.valueOf(playerId));
        try {
            if(auth==null || !auth.isAuthenticated()) {
                throw new IllegalArgumentException("Not authenticated Authentication:"+auth);
            }
            String username = auth.getName();
            PlayerDto playerDto = footballRoot.getPlayer(playerId);
            List<PlayerCustomPhotoDto> allPhotos = footballPreferenceService.getAllPhotosOfPlayerIncludeInactive(username, playerId);
            PlayerPhotosResponse[] responseArr = {FootballCustomPhotoMapper.toPlayerPhotosResponse(playerDto, allPhotos)};
            return apiResponseService.createSuccessResponse(responseArr, requestUrl, params);
        } catch (Exception e) {
            log.error("Failed to get player registered images", e);
            return apiResponseService.createFailureResponse("failed to get player registered images", requestUrl, params);
        }
    }

    public ApiResponse<String> uploadPlayerImage(Authentication auth, long playerId, MultipartFile photoFile, String requestUrl) {
        Map<String, String> params = Map.of("playerId", String.valueOf(playerId));
        try {
            if(auth==null || !auth.isAuthenticated()) {
                throw new IllegalArgumentException("Not authenticated Authentication:"+auth);
            }

            String username = auth.getName();
            footballPreferenceService.savePlayerCustomPhoto(username, playerId, photoFile);
            String[] responseArr = {"success"};
            return apiResponseService.createSuccessResponse(responseArr, requestUrl, params);
        } catch (Exception e) {
            log.error("Failed to upload player image", e);
            return apiResponseService.createFailureResponse("failed to upload player image", requestUrl, params);
        }
    }

    public ApiResponse<String> activateImage(Authentication auth, long playerId, long photoId, String requestUrl) {
        Map<String, String> params = Map.of("photoId", String.valueOf(photoId));
        try {
            if(auth==null || !auth.isAuthenticated()) {
                throw new IllegalArgumentException("Not authenticated Authentication:"+auth);
            }
            String username = auth.getName();
            boolean success = footballPreferenceService.activatePhoto(username, playerId, photoId);
            String[] responseArr = {success ? "success" : "failed"};
            return apiResponseService.createSuccessResponse(responseArr, requestUrl, params);
        } catch (Exception e) {
            log.error("Failed to activate image", e);
            return apiResponseService.createFailureResponse("failed to activate image", requestUrl, params);
        }
    }

    public ApiResponse<String> deactivateImage(Authentication auth, long playerId, long photoId, String requestUrl) {
        Map<String, String> params = Map.of("photoId", String.valueOf(photoId));
        try {
            if(auth==null || !auth.isAuthenticated()) {
                throw new IllegalArgumentException("Not authenticated Authentication:"+auth);
            }
            String username = auth.getName();
            boolean success = footballPreferenceService.deactivatePhoto(username, playerId, photoId);
            String[] responseArr = {success ? "success" : "failed"};
            return apiResponseService.createSuccessResponse(responseArr, requestUrl, params);
        } catch (Exception e) {
            log.error("Failed to deactivate image", e);
            return apiResponseService.createFailureResponse("failed to deactivate image", requestUrl, params);
        }
    }
}
