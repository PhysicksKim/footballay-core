package com.gyechunsik.scoreboard.domain.football.preference;

import com.gyechunsik.scoreboard.domain.football.preference.dto.PlayerCustomPhotoDto;
import com.gyechunsik.scoreboard.domain.football.preference.service.PlayerCustomPhotoService;
import com.gyechunsik.scoreboard.domain.user.entity.User;
import com.gyechunsik.scoreboard.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Football Domain 에서 Preference 관련 도메인 진입점입니다. <br>
 * Admin Page 에서 진입하는 경우 Username 으로 User - PreferenceKey 를 인증하며 이외의 경우 KeyHash - PreferenceKey 를 인증합니다. <br>
 * 진입점에 해당하므로 {@link Transactional} 을 사용하지 않습니다. <br>
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class FootballPreferenceService {

    private final PlayerCustomPhotoService playerCustomPhotoService;
    private final UserService userService;

    /**
     * admin page 에서 접근
     * @param username
     * @param playerId
     * @param photoFile
     */
    public void savePlayerCustomPhoto(String username, long playerId, MultipartFile photoFile) {
        User user = userService.findUser(username);
        PlayerCustomPhotoDto dto = playerCustomPhotoService.registerAndUploadCustomPhoto(user.getId(), playerId, photoFile);
        log.info("PlayerCustomPhoto saved. playerId={}, photoUrl={}", playerId, dto.getPhotoUrl());
    }

    /**
     * admin page 에서 팀 선수들의 커스텀 사진 조회
     * @param username
     * @param teamId
     * @return playerId - photoUrl map
     */
    public Map<Long, String> getSquadActiveCustomPhotos(String username, long teamId) {
        Map<Long, PlayerCustomPhotoDto> photoMap =
                playerCustomPhotoService.getActiveCustomPhotosWithUsernameAndTeamId(username, teamId);
        return extractPhotoUrlMap(photoMap);
    }

    public Map<Long, String> getAllPlayerCustomPhotoUrls(String keyHash, Set<Long> playerIds) {
        Map<Long, PlayerCustomPhotoDto> photoMap =
                playerCustomPhotoService.getActiveCustomPhotos(keyHash, playerIds);
        return extractPhotoUrlMap(photoMap);
    }

    public List<PlayerCustomPhotoDto> getAllPhotosOfPlayerIncludeInactive(String username, long playerId) {
        return playerCustomPhotoService.getAllCustomPhotosWithUsername(username, playerId);
    }

    public boolean activatePhoto(String username, long playerId, long photoId) {
        try {
            playerCustomPhotoService.activatePhotoWithUsername(username, playerId, photoId);
            return true;
        } catch (Exception e) {
            log.error("Activate photo failed. playerId={}, photoId={}", playerId, photoId, e);
            return false;
        }
    }

    public boolean deactivatePhoto(String username, long playerId, long photoId) {
        boolean success = playerCustomPhotoService.deactivatePhotoWithUsername(username, playerId, photoId);
        log.info("Deactivate photo success={}", success);
        return success;
    }

    public boolean deletePhoto(String username, long photoId) {
        boolean success = playerCustomPhotoService.deletePhotoWithUsername(username, photoId);
        log.info("Delete photo success={}", success);
        return success;
    }

    private static Map<Long, String> extractPhotoUrlMap(Map<Long, PlayerCustomPhotoDto> photoMap) {
        Map<Long, String> photoUrls = new HashMap<>();
        for (Map.Entry<Long, PlayerCustomPhotoDto> entry : photoMap.entrySet()) {
            photoUrls.put(entry.getKey(), entry.getValue().getPhotoUrl());
        }
        return photoUrls;
    }
}
