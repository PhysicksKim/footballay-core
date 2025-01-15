package com.gyechunsik.scoreboard.web.admin.football.controller;

import com.gyechunsik.scoreboard.domain.football.preference.persistence.PreferenceKey;
import com.gyechunsik.scoreboard.web.admin.football.response.PlayerPhotosResponse;
import com.gyechunsik.scoreboard.web.admin.football.response.PlayerResponse;
import com.gyechunsik.scoreboard.web.admin.football.response.PreferenceKeyResponse;
import com.gyechunsik.scoreboard.web.admin.football.service.AdminFootballPreferenceService;
import com.gyechunsik.scoreboard.web.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/football")
@PreAuthorize("hasRole('ADMIN')")
public class AdminFootballCustomPhotoController {

    private final AdminFootballPreferenceService preferenceService;

    private static final String CONTROLLER_URL = "/api/admin/football";

    /**
     * {@link PreferenceKey} 생성
     * @param auth
     * @return
     */
    @PostMapping("/preference")
    public ResponseEntity<?> createPreferenceKey(Authentication auth) {
        final String requestUrl = CONTROLLER_URL + "/preference";
        ApiResponse<PreferenceKeyResponse> preferenceKey = preferenceService.createPreferenceKey(auth, requestUrl);
        if(preferenceKey.metaData().responseCode() == 200) {
            String generatedKeyHash = preferenceKey.response()[0].keyHash();
            log.info("PreferenceKey auth:{}, hash:{}", auth, generatedKeyHash);
            return ResponseEntity.ok().body(preferenceKey);
        }
        return ResponseEntity.badRequest().body(preferenceKey);

    }

    /**
     * {@link PreferenceKey} 삭제
     * @param auth
     * @return
     */
    @DeleteMapping("/preference")
    public ResponseEntity<?> deletePreferenceKey(Authentication auth) {
        final String requestUrl = CONTROLLER_URL + "/preference";
        ApiResponse<String> response = preferenceService.deletePreferenceKey(auth, requestUrl);
        log.info("PreferenceKey Deleted: {}", response);
        return ResponseEntity.ok().body(response);
    }


    /**
     * 팀의 선수단 정보를 가져오고, 해당 유저의 커스텀 활성 이미지들을 포함하여 반환
     * GET /api/admin/football/teams/{teamId}/squad/custom
     *
     * @param auth   인증 객체
     * @param teamId 팀 ID
     * @return ResponseEntity with Squad Info and Custom Photos
     */
    @GetMapping("/teams/{teamId}/squad/custom")
    public ResponseEntity<?> getSquadWithCustomPhotos(
            Authentication auth,
            @PathVariable long teamId
    ) {
        final String requestUrl = CONTROLLER_URL + "/teams/" + teamId + "/squad/custom";
        ApiResponse<PlayerResponse> response = preferenceService.getSquadCustomPhotos(auth, teamId, requestUrl);
        log.info("Squad with Custom Photos: {}", response);
        return ResponseEntity.ok().body(response);
    }

    /**
     * 특정 선수의 등록된 이미지 목록을 가져옴 (active 및 inactive 포함)
     * GET /api/admin/football/players/{playerId}/images
     *
     * @param auth     인증 객체
     * @param playerId 선수 ID
     * @return ResponseEntity with List of Registered Images
     */
    @GetMapping("/players/{playerId}/images")
    public ResponseEntity<?> getPlayerRegisteredImages(
            Authentication auth,
            @PathVariable long playerId
    ) {
        final String requestUrl = CONTROLLER_URL + "/players/" + playerId + "/images";
        ApiResponse<PlayerPhotosResponse> response = preferenceService.getPlayerRegisteredImages(auth, playerId, requestUrl);
        log.info("Player Registered Images: {}", response);
        return ResponseEntity.ok().body(response);
    }

    /**
     * 특정 선수의 이미지를 새롭게 등록 및 업로드하고, 해당 이미지를 active로 설정
     * POST /api/admin/football/players/{playerId}/images
     *
     * @param auth     인증 객체
     * @param playerId 선수 ID
     * @param photo    업로드할 이미지 파일
     * @return ResponseEntity indicating success or failure
     */
    @PostMapping("/players/{playerId}/images")
    public ResponseEntity<?> uploadPlayerImage(
            Authentication auth,
            @PathVariable long playerId,
            @RequestPart MultipartFile photo
    ) {
        final String requestUrl = CONTROLLER_URL + "/players/" + playerId + "/images";
        ApiResponse<String> response = preferenceService.uploadPlayerImage(auth, playerId, photo, requestUrl);
        log.info("Player Image Uploaded: {}", response);
        return ResponseEntity.status(201).build();
    }

    /**
     * 특정 이미지를 활성화하고, 다른 이미지는 비활성화
     * PATCH /api/admin/football/images/{photoId}/activate
     *
     * @param auth    인증 객체
     * @param photoId 이미지 ID
     * @return ResponseEntity indicating success or failure
     */
    @PatchMapping("/players/{playerId}/images/{photoId}/activate")
    public ResponseEntity<?> activateImage(
            Authentication auth,
            @PathVariable long playerId,
            @PathVariable long photoId
    ) {
        final String requestUrl = CONTROLLER_URL + "/players/" + playerId + "/images/" + photoId + "/activate";
        ApiResponse<String> response = preferenceService.activateImage(auth, playerId, photoId, requestUrl);
        log.info("Image Activated: {}", response);
        return ResponseEntity.ok().build();
    }

    /**
     * 특정 이미지를 비활성화하고, 기본 이미지를 사용하도록 설정
     * PATCH /api/admin/football/images/{photoId}/deactivate
     *
     * @param auth    인증 객체
     * @param photoId 이미지 ID
     * @return ResponseEntity indicating success or failure
     */
    @PatchMapping("/players/{playerId}/images/{photoId}/deactivate")
    public ResponseEntity<?> deactivateImage(
            Authentication auth,
            @PathVariable long playerId,
            @PathVariable long photoId
    ) {
        final String requestUrl = CONTROLLER_URL + "/players/" + playerId + "/images/" + photoId + "/deactivate";
        ApiResponse<String> response = preferenceService.deactivateImage(auth, playerId, photoId, requestUrl);
        log.info("Image Deactivated: {}", response);
        return ResponseEntity.ok().build();
    }
}
