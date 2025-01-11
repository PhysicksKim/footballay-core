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

// TODO : s3 upload 과정과 entity 저장 과정을 분리해야합니다. 파일 업로드 시간동안 db connection 점유 문제가 있습니다.
@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerCustomPhotoService {

    private final PlayerCustomPhotoRepository PlayerCustomPhotoRepository;
    private final UserRepository userRepository;
    private final PlayerRepository playerRepository;
    private final PreferenceKeyRepository preferenceKeyRepository;
    private final PlayerCustomPhotoRepository playerCustomPhotoRepository;
    private final UserFilePathService userFilePathService;
    private final PreferenceValidator preferenceValidator;
    private final S3Uploader s3Uploader;

    /**
     * 새로운 커스텀 선수 이미지를 등록하고 업로드이후 활성화합니다. <br>
     * 기존에 활성화된 커스텀 선수 이미지가 있으면 비활성화하고 새로운 이미지를 활성화 시킵니다. <br>
     *
     * @throws IllegalArgumentException 이미지 파일이 유효하지 않은 경우
     * @param userId    사용자 ID
     * @param playerId  선수 ID
     * @param file      업로드할 이미지 파일
     * @return 등록된 커스텀 선수 이미지 DTO
     */
    @Transactional
    public PlayerCustomPhotoDto registerAndUploadCustomPhoto(long userId, long playerId, MultipartFile file) {
        if(!validateCustomPhoto(file)) {
            throw new IllegalArgumentException("Invalid custom photo");
        }

        PreferenceKey preferenceKey = getPreferenceKeyOrThrow(userId);
        UserFilePath nowUserFilePath = getUserFilePathForNowPhoto(userId);
        String fileName = generateFileName(playerId, file);
        PlayerCustomPhoto playerCustomPhoto = createPlayerCustomPhotoEntity(
                preferenceKey,
                nowUserFilePath,
                playerId,
                fileName
        );

        deactivatePreviousPhoto(preferenceKey, playerId);

        String s3Key = getS3Key(nowUserFilePath, fileName);
        s3Uploader.uploadFile(file, s3Key);

        return PlayerCustomPhotoDto.fromEntity(playerCustomPhotoRepository.save(playerCustomPhoto));
    }

    /**
     * 활성화된 커스텀 선수 이미지들을 조회합니다. <br>
     * 선수 ID 집합에 대해 활성화된 커스텀 선수 이미지를 조회합니다. <br>
     * 커스텀 이미지가 없는 선수는 반환되는 Map에 포함되지 않습니다.
     *
     * @param preferenceKey 커스텀 선수 이미지를 조회할 PreferenceKey
     * @param playerIds 조회할 선수들의 ID 집합
     * @return 활성화된 커스텀 선수 이미지 DTO
     */
    @Transactional(readOnly = true)
    public Map<Long, PlayerCustomPhotoDto> getActiveCustomPhotos(String preferenceKey, Set<Long> playerIds) {
        Optional<PreferenceKey> optionalKey = preferenceKeyRepository.findByKeyhash(preferenceKey);
        if(optionalKey.isEmpty()) {
            throw new IllegalArgumentException("PreferenceKey not found with key: " + preferenceKey);
        }
        PreferenceKey key = optionalKey.get();

        List<PlayerCustomPhoto> photos = PlayerCustomPhotoRepository.findAllActivesByPreferenceKeyAndPlayers(
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
     * 활성 비활성 둘 다 포함해 커스텀 선수 이미지들을 모두 조회합니다.
     * @param preferenceKey PreferenceKey
     * @param playerId 선수 ID
     * @return 모든 커스텀 선수 이미지 DTO
     */
    @Transactional
    public List<PlayerCustomPhotoDto> getAllCustomPhotos(String preferenceKey, long playerId) {
        PreferenceKey key = preferenceKeyRepository.findByKeyhash(preferenceKey)
                .orElseThrow(() -> new IllegalArgumentException("PreferenceKey not found with key: " + preferenceKey));

        List<PlayerCustomPhoto> photoList = playerCustomPhotoRepository.findAllByPreferenceKeyAndPlayer(key, playerId);
        return photoList.stream()
                .map(PlayerCustomPhotoDto::fromEntity)
                .collect(Collectors.toList());
    }

    // TODO : DeactivatePhoto(), activatePhoto(), deletePhoto() method 추가 필요
    /**
     * 유저의 특정 커스텀 이미지를 비활성화합니다. <br>
     * 이미지가 존재하지 않는 경우에도 true 반환합니다. <br>
     * {@link PlayerCustomPhoto} 엔티티의 uniqueConstraints 에 따라서 keyHash 와 playerId 가 주어지면 활성화된 엔티티는 고유합니다.
     *
     * @param keyHash 커스텀 이미지 PreferenceKey
     * @param playerId 선수 ID
     * @return 비활성화 성공 여부. 기존에 활성화된 이미지가 없더라도 true 반환
     */
    @Transactional
    public boolean deactivatePhoto(String keyHash, long playerId, long photoId) {
        try {
            Optional<PlayerCustomPhoto> optionalPhoto = playerCustomPhotoRepository.findById(photoId);
            if(optionalPhoto.isEmpty()) {
                log.info("Photo not found with id={}", photoId);
                return true;
            }

            PlayerCustomPhoto photo = optionalPhoto.get();
            validatePhotoKeyHashAndPlayerId(photo, keyHash, playerId);

            log.info("Deactivating photo id={}", photo.getId());
            photo.setActive(false);
            return true;
        } catch (Exception e) {
            log.error("Failed to deactivate photo", e);
            return false;
        }
    }

    /**
     * 일치하지 않는다면 비정상적인 접근입니다.
     *
     * @param entity
     * @param keyHash
     */
    private void validatePhotoKeyHashAndPlayerId(PlayerCustomPhoto entity, String keyHash, long playerId) {
        if(!entity.getPreferenceKey().getKeyhash().equals(keyHash)) {
            throw new IllegalArgumentException("Photo preferenceKey is not matched with keyHash: " + keyHash);
        }
        if(entity.getPlayer().getId() != playerId) {
            throw new IllegalArgumentException("Photo playerId is not matched with playerId: " + playerId);
        }
    }

    // custom photo 중 active 이미지를 변경하기 위해 사용
    /**
     * 유저의 특정 커스텀 이미지를 활성화합니다. <br>
     * 기존 활성화된 이미지가 있으면 비활성화 하고 새로운 이미지를 활성화합니다.
     *
     * @throws IllegalArgumentException 이미지가 존재하지 않거나 keyHash 와 photoId 가 일치하지 않는 경우
     * @param keyHash 커스텀 이미지 PreferenceKey
     * @param playerId 활성화할 선수 ID
     * @param photoId 활성화할 커스텀 이미지 ID
     * @return 활성화된 커스텀 선수 이미지 DTO
     */
    @Transactional
    public PlayerCustomPhotoDto activatePhoto(String keyHash, long playerId, long photoId) {
        PreferenceKey preferenceKey = preferenceKeyRepository.findByKeyhash(keyHash)
                .orElseThrow(() -> new IllegalArgumentException("PreferenceKey not found with key: " + keyHash));

        // 기존 활성화된 이미지가 있으면 비활성화
        playerCustomPhotoRepository.findActivePhotoByPreferenceKeyAndPlayer(preferenceKey, playerId)
                .ifPresent(activePhoto -> {
                    log.info("Deactivating active photo id={}", activePhoto.getId());
                    activePhoto.setActive(false);
                });

        // 새로운 이미지 활성화
        PlayerCustomPhoto photo = playerCustomPhotoRepository.findById(photoId)
                .orElseThrow(() -> new IllegalArgumentException("PlayerCustomPhoto not found with id: " + photoId));
        if(!photo.getPreferenceKey().getKeyhash().equals(keyHash)) {
            throw new IllegalArgumentException("Photo preferenceKey is not matched with keyHash: " + keyHash);
        }
        photo.setActive(true);

        return PlayerCustomPhotoDto.fromEntity(photo);
    }

    // custom photo 엔티티 및 파일을 삭제하기 위해 사용
    /**
     * 유저의 특정 커스텀 이미지를 삭제합니다. <br>
     * 이미지가 존재하지 않는 경우에도 true 반환합니다.
     *
     * @throws IllegalArgumentException 이미지가 존재하지 않거나 keyHash 와 photoId 가 일치하지 않는 경우
     * @param preferenceKey PreferenceKey
     * @param photoId 삭제할 커스텀 이미지 ID
     * @return 삭제 성공 여부. 이미지가 존재하지 않는 경우에도 true 반환
     */
    @Transactional
    public boolean deletePhoto(String preferenceKey, long photoId) {
        try {
            PreferenceKey key = preferenceKeyRepository.findByKeyhash(preferenceKey)
                    .orElseThrow(() -> new IllegalArgumentException("PreferenceKey not found with key: " + preferenceKey));
            playerCustomPhotoRepository.deleteByIdAndPreferenceKey(photoId, key);
            return true;
        } catch (Exception e) {
            log.error("Failed to delete photo", e);
            return false;
        }
    }

    private PreferenceKey getPreferenceKeyOrThrow(long userId) {
        return preferenceKeyRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("PreferenceKey not found for userId: " + userId));
    }

    private static String generateFileName(long playerId, MultipartFile file) {
        String fileExtension = FilenameUtils.getExtension(file.getOriginalFilename());
        validateFileExtension(fileExtension);
        String fileName = CustomPhotoFileNameGenerator.generate(playerId, fileExtension);
        return fileName;
    }

    private UserFilePath getUserFilePathForNowPhoto(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        return userFilePathService.getPlayerCustomPhotoPath(user);
    }

    private void deactivatePreviousPhoto(PreferenceKey preferenceKey, long playerId) {
        playerCustomPhotoRepository.findActivePhotoByPreferenceKeyAndPlayer(preferenceKey, playerId)
                        .ifPresent(photo -> {
                            log.info("Active photo already exists of preferenceKey.id={} Deactivating photo id={}", preferenceKey.getId(), photo.getId());
                            photo.setActive(false);
                        });
        playerCustomPhotoRepository.flush();
    }

    private static void validateFileExtension(String fileExtension) {
        if(fileExtension == null || fileExtension.isEmpty()) {
            throw new IllegalArgumentException("Invalid file extension");
        }
    }

    private PlayerCustomPhoto createPlayerCustomPhotoEntity(PreferenceKey preferenceKey, UserFilePath userFilePath, long playerId, String fileName) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found with id: " + playerId));
        return PlayerCustomPhoto.builder()
                .preferenceKey(preferenceKey)
                .userFilePath(userFilePath)
                .player(player)
                .fileName(fileName)
                .isActive(true)
                .build();
    }

    private static String getS3Key(UserFilePath userFilePathForCustomPhoto, String fileName) {
        return userFilePathForCustomPhoto.getPathWithoutDomain() + fileName;
    }

    private boolean validateCustomPhoto(MultipartFile file) {
        return preferenceValidator.isValidPlayerCustomPhotoImage(file);
    }

}