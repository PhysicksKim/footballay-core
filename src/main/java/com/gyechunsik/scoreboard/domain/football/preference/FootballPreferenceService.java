package com.gyechunsik.scoreboard.domain.football.preference;

import com.gyechunsik.scoreboard.domain.football.preference.dto.PlayerCustomPhotoDto;
import com.gyechunsik.scoreboard.domain.football.preference.service.PlayerCustomPhotoService;
import com.gyechunsik.scoreboard.domain.user.entity.User;
import com.gyechunsik.scoreboard.domain.user.repository.UserRepository;
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
 * Football Domain 에서 Preference 관련 도메인 진입점입니다.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class FootballPreferenceService {

    private final PlayerCustomPhotoService playerCustomPhotoService;
    private final UserService userService;

    public void savePlayerCustomPhoto(String username, long playerId, MultipartFile photoFile) {
        User user = userService.findUser(username);
        PlayerCustomPhotoDto dto = playerCustomPhotoService.registerAndUploadCustomPhoto(user.getId(), playerId, photoFile);
        log.info("PlayerCustomPhoto saved. playerId={}, photoUrl={}", playerId, dto.getPhotoUrl());
    }

    public Map<Long, String> getAllPlayerCustomPhotoUrls(String keyHash, Set<Long> playerIds) {
        Map<Long, PlayerCustomPhotoDto> photoMap =
                playerCustomPhotoService.getActiveCustomPhotos(keyHash, playerIds);
        return extractPhotoUrlMap(photoMap);
    }

    public List<PlayerCustomPhotoDto> getAllPhotosOfPlayerIncludeInactive(String keyHash, long playerId) {
        return playerCustomPhotoService.getAllCustomPhotos(keyHash, playerId);
    }

    public void activatePhoto(String keyHash, long playerId, long photoId) {
        playerCustomPhotoService.activatePhoto(keyHash, playerId, photoId);
    }

    public void deactivatePhoto(String keyHash, long playerId, long photoId) {
        playerCustomPhotoService.deactivatePhoto(keyHash, playerId, photoId);
    }

    public void deletePhoto(String keyHash, long photoId) {
        playerCustomPhotoService.deletePhoto(keyHash, photoId);
    }

    private static Map<Long, String> extractPhotoUrlMap(Map<Long, PlayerCustomPhotoDto> photoMap) {
        Map<Long, String> photoUrls = new HashMap<>();
        for (Map.Entry<Long, PlayerCustomPhotoDto> entry : photoMap.entrySet()) {
            photoUrls.put(entry.getKey(), entry.getValue().getPhotoUrl());
        }
        return photoUrls;
    }
}
