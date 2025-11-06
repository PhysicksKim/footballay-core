package com.footballay.core.domain.football.preference;

import com.footballay.core.domain.football.preference.dto.PlayerCustomPhotoDto;
import com.footballay.core.domain.football.preference.persistence.PreferenceKey;
import com.footballay.core.domain.football.preference.service.PlayerCustomPhotoService;
import com.footballay.core.domain.football.preference.service.PreferenceKeyService;
import com.footballay.core.domain.user.entity.User;
import com.footballay.core.domain.user.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;

/**
 * Football Domain 에서 Preference <b>도메인 진입점</b>입니다. <br>
 * 읽기 요청의 경우 {@link PreferenceKey#getKeyhash()} 를 사용하며, 쓰기 요청의 경우 인증된 유저의 username 이 필요합니다. <br>
 * Admin Page 에서 진입하는 경우 Username 으로 User - PreferenceKey 를 인증하며 이외의 경우 KeyHash - PreferenceKey 를 인증합니다. <br>
 * 진입점에 해당하므로 {@link Transactional} 을 사용하지 않습니다. <br>
 */
@Service
public class FootballPreferenceService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FootballPreferenceService.class);
    private final PreferenceKeyService preferenceKeyService;
    private final PlayerCustomPhotoService playerCustomPhotoService;
    private final UserService userService;

    public boolean validatePreferenceKey(String keyHash) {
        log.info("Validating preferenceKey={}", keyHash);
        return preferenceKeyService.validateKeyHash(keyHash);
    }

    /**
     * PreferenceKey 를 생성합니다.
     *
     * @param username Authenticated 된 User 의 username
     * @return 생성된 keyHash
     */
    public String createPreferenceKey(String username) {
        User user = userService.findUser(username);
        Optional<PreferenceKey> optionKey = preferenceKeyService.findPreferenceKey(user);
        if (optionKey.isPresent()) {
            throw new IllegalStateException("PreferenceKey already exists for user=" + username);
        }
        PreferenceKey preferenceKey = preferenceKeyService.generatePreferenceKeyForUser(user);
        return preferenceKey.getKeyhash();
    }

    /**
     * PreferenceKey 를 재발급합니다.
     *
     * @param username Authenticated 된 User 의 username
     * @return 새로 생성된 keyHash
     */
    public String reissuePreferenceKey(String username) {
        User user = userService.findUser(username);
        PreferenceKey preferenceKey = preferenceKeyService.reissuePreferenceKeyForUser(user);
        log.info("Reissued preferenceKey={} for user=[name={},id={}]", preferenceKey.getKeyhash(), user.getUsername(), user.getId());
        return preferenceKey.getKeyhash();
    }

    /**
     * PreferenceKey 를 삭제합니다.
     *
     * @param username Authenticated 된 User 의 username
     * @return 존재하지 않으면 false, 삭제 성공하면 true
     */
    public boolean deletePreferenceKey(String username) {
        User user = userService.findUser(username);
        return preferenceKeyService.deletePreferenceKeyForUser(user);
    }

    /**
     * 선수의 커스텀 사진을 저장합니다. <br>
     * Authenticated 된 User 의 username 을 사용해야 합니다. <br>
     * {@link Authentication} 객체의 {@link Authentication#isAuthenticated()} 된 객체에서 {@link Authentication#getName()} 을 사용합니다.<br>
     *
     * @param username  Authenticated 된 User 의 username
     * @param playerId  선수 ID
     * @param photoFile 선수 사진 파일
     */
    public void savePlayerCustomPhoto(String username, long playerId, MultipartFile photoFile) {
        User user = userService.findUser(username);
        PlayerCustomPhotoDto dto = playerCustomPhotoService.registerAndUploadCustomPhoto(user.getId(), playerId, photoFile);
        log.info("PlayerCustomPhoto saved. playerId={}, photoUrl={}", playerId, dto.getPhotoUrl());
    }

    /**
     * admin page 에서 팀 선수들의 커스텀 사진 조회
     *
     * @param username
     * @param teamId
     * @return playerId - photoUrl map
     */
    public Map<Long, String> getSquadActiveCustomPhotos(String username, long teamId) {
        Map<Long, PlayerCustomPhotoDto> photoMap = playerCustomPhotoService.getActiveCustomPhotosWithUsernameAndTeamId(username, teamId);
        return extractPhotoUrlMap(photoMap);
    }

    /**
     * 선수의 커스텀 사진을 조회합니다. <br>
     * keyHash 가 유효하지 않아 조회되는 데이터가 없다면 빈 Map 을 반환합니다. <br>
     *
     * @param keyHash
     * @param playerIds
     * @return
     */
    public Map<Long, String> getCustomPhotoUrlsOfPlayers(String keyHash, Set<Long> playerIds) {
        Map<Long, PlayerCustomPhotoDto> photoMap = playerCustomPhotoService.getActiveCustomPhotos(keyHash, playerIds);
        return extractPhotoUrlMap(photoMap);
    }

    public List<PlayerCustomPhotoDto> getAllPhotosOfPlayerIncludeInactive(String username, long playerId) {
        return playerCustomPhotoService.getAllCustomPhotosWithUsername(username, playerId);
    }

    public boolean activatePhoto(String username, long photoId) {
        try {
            playerCustomPhotoService.activatePhotoWithUsername(username, photoId);
            return true;
        } catch (Exception e) {
            log.error("Activate photo failed. photoId={}", photoId, e);
            return false;
        }
    }

    public boolean deactivatePhoto(String username, long photoId) {
        try {
            boolean success = playerCustomPhotoService.deactivatePhotoWithUsername(username, photoId);
            log.info("Deactivate photo success={}", success);
            return success;
        } catch (Exception e) {
            log.error("Deactivate photo failed. photoId={}", photoId, e);
            return false;
        }
    }

    @PreAuthorize("hasAnyRole({\'ADMIN\', \'STREAMER\'})")
    public boolean switchToDefaultPhoto(long playerId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return playerCustomPhotoService.deactivatePhotoWithUsernameAndPlayerId(username, playerId);
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

    public FootballPreferenceService(final PreferenceKeyService preferenceKeyService, final PlayerCustomPhotoService playerCustomPhotoService, final UserService userService) {
        this.preferenceKeyService = preferenceKeyService;
        this.playerCustomPhotoService = playerCustomPhotoService;
        this.userService = userService;
    }
}
