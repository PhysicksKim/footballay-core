package com.gyechunsik.scoreboard.domain.football.preference.service;

import com.gyechunsik.scoreboard.domain.football.persistence.Player;
import com.gyechunsik.scoreboard.domain.football.preference.dto.PlayerCustomPhotoDto;
import com.gyechunsik.scoreboard.domain.football.preference.persistence.PlayerCustomPhoto;
import com.gyechunsik.scoreboard.domain.football.preference.persistence.PreferenceKey;
import com.gyechunsik.scoreboard.domain.football.preference.persistence.UserFilePath;
import com.gyechunsik.scoreboard.domain.football.preference.repository.PlayerCustomPhotoRepository;
import com.gyechunsik.scoreboard.domain.football.preference.repository.PreferenceKeyRepository;
import com.gyechunsik.scoreboard.domain.football.preference.util.CustomPhotoFileNameGenerator;
import com.gyechunsik.scoreboard.domain.football.preference.util.PreferenceValidator;
import com.gyechunsik.scoreboard.domain.football.preference.util.S3Uploader;
import com.gyechunsik.scoreboard.domain.football.repository.PlayerRepository;
import com.gyechunsik.scoreboard.entity.user.User;
import com.gyechunsik.scoreboard.entity.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerCustomPhotoService {

    private final PlayerCustomPhotoRepository PlayerCustomPhotoRepository;
    private final UserRepository userRepository;
    private final PlayerRepository playerRepository;
    private final PreferenceKeyRepository preferenceKeyRepository;
    private final PreferenceValidator preferenceValidator;
    private final S3Uploader s3Uploader;
    private final PlayerCustomPhotoRepository playerCustomPhotoRepository;
    private final UserFilePathService userFilePathService;

    /**
     * 활성화된 커스텀 선수 이미지들을 조회합니다.
     *
     * @param preferenceKey 커스텀 선수 이미지를 조회할 PreferenceKey
     * @param playerIds 조회할 선수들의 ID 집합
     * @return 활성화된 커스텀 선수 이미지 DTO
     */
    @Transactional(readOnly = true)
    public Map<Long, PlayerCustomPhotoDto> getPlayerCustomPhotos(String preferenceKey, Set<Long> playerIds) {
        Optional<PreferenceKey> optionalKey = preferenceKeyRepository.findByKeyhash(preferenceKey);
        if(optionalKey.isEmpty()) {
            throw new IllegalArgumentException("PreferenceKey not found with key: " + preferenceKey);
        }
        PreferenceKey key = optionalKey.get();

        List<PlayerCustomPhoto> photos = PlayerCustomPhotoRepository.findActivePhotosByPreferenceKeyAndPlayers(
                key.getId(), playerIds);

        Map<Long, PlayerCustomPhoto> photoMap = new HashMap<>();
        for(PlayerCustomPhoto photo : photos) {
            Player player = photo.getPlayer();

            // 활성화된 이미지가 둘 이상인 경우 가장 마지막에 수정된 이미지를 선택
            if (photoMap.containsKey(player.getId())) {
                log.warn("Multiple active custom photos found for player={},preferenceKey={}", player.getId(), preferenceKey);
                PlayerCustomPhoto existingPhoto = photoMap.get(player.getId());
                if (existingPhoto.getModifiedDate().isBefore(photo.getModifiedDate())) {
                    photoMap.put(player.getId(), photo);
                    existingPhoto.setActive(false);
                } else {
                    photo.setActive(false);
                }
            } else {
                photoMap.put(player.getId(), photo);
            }
        }

        Map<Long, PlayerCustomPhotoDto> photoDtoMap = photoMap.values().stream()
                .map(PlayerCustomPhotoDto::fromEntity)
                .collect(Collectors.toMap(PlayerCustomPhotoDto::getPlayerId, photo -> photo));
        return photoDtoMap;
    }

    /**
     * 새로운 커스텀 선수 이미지를 등록합니다.
     *
     * @param userId    사용자 ID
     * @param playerId  선수 ID
     * @param file      업로드할 이미지 파일
     * @return 등록된 커스텀 선수 이미지 DTO
     */
    @Transactional
    public PlayerCustomPhotoDto registerCustomPhoto(Long userId, Long playerId, MultipartFile file) {
        if(!validateCustomPhoto(file)) {
            throw new IllegalArgumentException("Invalid custom photo");
        }

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found with id: " + playerId));
        PreferenceKey preferenceKey = preferenceKeyRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("PreferenceKey not found for userId: " + userId));

        List<PlayerCustomPhoto> activePhotos =
                playerCustomPhotoRepository.findActivePhotosByPreferenceKeyAndPlayer(preferenceKey.getId(), playerId);
        for(PlayerCustomPhoto photo : activePhotos) {
            photo.setActive(false);
        }

        int version = 1;

        // 해당 키와 선수에 이미지가 있다면 version 가장 큰 값을 가져옴
        Optional<PlayerCustomPhoto> latestPhoto = playerCustomPhotoRepository.findLatestPhotoByPreferenceKeyAndPlayer(
                preferenceKey.getId(), playerId);
        if(latestPhoto.isPresent()) {
            version = latestPhoto.get().getVersion() + 1;
        }

        // 해당 키와 선수에 이미 활성화된 이미지 있다면 비활성화 처리
        playerCustomPhotoRepository.findActivePhotosByPreferenceKeyAndPlayer(preferenceKey.getId(), playerId)
                .forEach(photo -> photo.setActive(false));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        UserFilePath userFilePathForCustomPhoto = userFilePathService.getPlayerCustomPhotoPath(user);

        String fileExtension = FilenameUtils.getExtension(file.getOriginalFilename());
        if(fileExtension == null || fileExtension.isEmpty()) {
            throw new IllegalArgumentException("Invalid file extension");
        }
        String fileName = CustomPhotoFileNameGenerator.generate(playerId, version, fileExtension);

        PlayerCustomPhoto playerCustomPhoto = PlayerCustomPhoto.builder()
                .preferenceKey(preferenceKey)
                .player(player)
                .userFilePath(userFilePathForCustomPhoto)
                .fileName(fileName)
                .version(version)
                .isActive(true)
                .build();

        return PlayerCustomPhotoDto.fromEntity(playerCustomPhotoRepository.save(playerCustomPhoto));
    }

    private boolean validateCustomPhoto(MultipartFile file) {
        preferenceValidator.isValidPlayerCustomPhotoImage(file);
        return false;
    }

}