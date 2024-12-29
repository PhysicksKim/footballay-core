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
import org.jetbrains.annotations.NotNull;
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

    // TODO : s3 upload 과정과 entity 저장 과정을 분리해야합니다. 파일 업로드 시간동안 db connection 점유 문제가 있습니다.
    /**
     * 새로운 커스텀 선수 이미지를 등록합니다.
     *
     * @param userId    사용자 ID
     * @param playerId  선수 ID
     * @param file      업로드할 이미지 파일
     * @return 등록된 커스텀 선수 이미지 DTO
     */
    @Transactional
    public PlayerCustomPhotoDto registerAndUploadCustomPhoto(Long userId, Long playerId, MultipartFile file) {
        if(!validateCustomPhoto(file)) {
            throw new IllegalArgumentException("Invalid custom photo");
        }

        PreferenceKey preferenceKey = getPreferenceKeyOrThrow(userId);
        UserFilePath nowUserFilePath = getUserFilePathForNowPhoto(userId);
        int version = getVersionForNewPhoto(preferenceKey, playerId);
        String fileName = generateFileName(playerId, file, version);
        PlayerCustomPhoto playerCustomPhoto = createPlayerCustomPhotoEntity(
                preferenceKey,
                nowUserFilePath,
                playerId,
                version,
                fileName
        );

        deactivePreviousPhotos(preferenceKey, playerId);

        String s3Key = getS3Key(nowUserFilePath, fileName);
        s3Uploader.uploadFile(file, s3Key);

        return PlayerCustomPhotoDto.fromEntity(playerCustomPhotoRepository.save(playerCustomPhoto));
    }

    private PreferenceKey getPreferenceKeyOrThrow(Long userId) {
        return preferenceKeyRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("PreferenceKey not found for userId: " + userId));
    }

    private static @NotNull String generateFileName(Long playerId, MultipartFile file, int version) {
        String fileExtension = FilenameUtils.getExtension(file.getOriginalFilename());
        validateFileExtension(fileExtension);
        String fileName = CustomPhotoFileNameGenerator.generate(playerId, version, fileExtension);
        return fileName;
    }

    private UserFilePath getUserFilePathForNowPhoto(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        return userFilePathService.getPlayerCustomPhotoPath(user);
    }

    private void deactivePreviousPhotos(PreferenceKey preferenceKey, Long playerId) {
        playerCustomPhotoRepository.findActivePhotosByPreferenceKeyAndPlayer(preferenceKey.getId(), playerId)
                .forEach(photo -> photo.setActive(false));
    }

    private int getVersionForNewPhoto(PreferenceKey preferenceKey, Long playerId) {
        Optional<PlayerCustomPhoto> latestPhoto = playerCustomPhotoRepository.findLatestPhotoByPreferenceKeyAndPlayer(
                preferenceKey.getId(), playerId);
        if(latestPhoto.isEmpty()) {
            return 1;
        } else {
            return latestPhoto.get().getVersion() + 1;
        }
    }

    private static void validateFileExtension(String fileExtension) {
        if(fileExtension == null || fileExtension.isEmpty()) {
            throw new IllegalArgumentException("Invalid file extension");
        }
    }

    private PlayerCustomPhoto createPlayerCustomPhotoEntity(PreferenceKey preferenceKey, UserFilePath userFilePath, long playerId, int version, String fileName) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found with id: " + playerId));
        return PlayerCustomPhoto.builder()
                .preferenceKey(preferenceKey)
                .userFilePath(userFilePath)
                .player(player)
                .fileName(fileName)
                .version(version)
                .isActive(true)
                .build();
    }

    private static String getS3Key(UserFilePath userFilePathForCustomPhoto, String fileName) {
        return userFilePathForCustomPhoto.getPathWithoutDomain() + fileName;
    }

    private boolean validateCustomPhoto(MultipartFile file) {
        preferenceValidator.isValidPlayerCustomPhotoImage(file);
        return false;
    }

}