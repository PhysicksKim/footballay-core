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
import com.gyechunsik.scoreboard.domain.user.entity.User;
import com.gyechunsik.scoreboard.domain.user.repository.UserRepository;
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
        PlayerCustomPhoto savedPhoto = playerCustomPhotoRepository.save(playerCustomPhoto);

        deactivatePreviousPhoto(preferenceKey, playerId);
        String s3Key = getS3Key(nowUserFilePath, fileName);
        s3Uploader.uploadFile(file, s3Key);

        return PlayerCustomPhotoDto.fromEntity(savedPhoto);
    }

    /**
     * 활성화된 커스텀 선수 이미지들을 조회합니다. <br>
     * 선수 ID 집합에 대해 활성화된 커스텀 선수 이미지를 조회합니다. <br>
     * 커스텀 이미지가 없는 선수는 반환되는 Map에 포함되지 않습니다.
     *
     * @param keyHash 커스텀 선수 이미지를 조회할 PreferenceKey
     * @param playerIds 조회할 선수들의 ID 집합
     * @return 활성화된 커스텀 선수 이미지 DTO
     */
    @Transactional(readOnly = true)
    public Map<Long, PlayerCustomPhotoDto> getActiveCustomPhotos(String keyHash, Set<Long> playerIds) {
        PreferenceKey key = getKey(keyHash);

        List<PlayerCustomPhoto> photos = playerCustomPhotoRepository.findAllActivesByPreferenceKeyAndPlayers(
                key.getId(), playerIds);
        photos = ensureSingleActivePhotosWithList(key, photos);

        return photos.stream()
                .map(PlayerCustomPhotoDto::fromEntity)
                .collect(Collectors.toMap(PlayerCustomPhotoDto::getPlayerId, photo -> photo));
    }

    /**
     * 활성 비활성 둘 다 포함해 커스텀 선수 이미지들을 모두 조회합니다.
     * @param preferenceKey PreferenceKey
     * @param playerId 선수 ID
     * @return 모든 커스텀 선수 이미지 DTO
     */
    @Transactional
    public List<PlayerCustomPhotoDto> getAllCustomPhotos(String preferenceKey, long playerId) {
        PreferenceKey key = getKey(preferenceKey);

        List<PlayerCustomPhoto> photoList = playerCustomPhotoRepository.findAllByPreferenceKeyAndPlayer(key, playerId);
        return photoList.stream()
                .map(PlayerCustomPhotoDto::fromEntity)
                .collect(Collectors.toList());
    }

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
        PreferenceKey preferenceKey = getKey(keyHash);

        deactivatePreviousPhoto(preferenceKey, playerId);

        PlayerCustomPhoto photo = playerCustomPhotoRepository.findById(photoId)
                .orElseThrow(() -> new IllegalArgumentException("PlayerCustomPhoto not found with id: " + photoId));
        if(!photo.getPreferenceKey().getKeyhash().equals(keyHash)) {
            throw new IllegalArgumentException("Photo preferenceKey is not matched with keyHash: " + keyHash);
        }
        photo.setActive(true);

        return PlayerCustomPhotoDto.fromEntity(photo);
    }

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
            PreferenceKey key = getKey(preferenceKey);
            playerCustomPhotoRepository.deleteByIdAndPreferenceKey(photoId, key);
            return true;
        } catch (Exception e) {
            log.error("Failed to delete photo", e);
            return false;
        }
    }

    private List<PlayerCustomPhoto> ensureSingleActivePhotosWithList(PreferenceKey preferenceKey, List<PlayerCustomPhoto> photos) {
        List<PlayerCustomPhoto> activePhotosAfterEnforcement = new ArrayList<>();

        Map<Long, List<PlayerCustomPhoto>> groupedPhotos = photos.stream()
                .collect(Collectors.groupingBy(photo -> photo.getPlayer().getId()));

        for (Map.Entry<Long, List<PlayerCustomPhoto>> entry : groupedPhotos.entrySet()) {
            Long playerId = entry.getKey();
            List<PlayerCustomPhoto> playerPhotos = entry.getValue();

            if (playerPhotos.size() <= 1) {
                activePhotosAfterEnforcement.addAll(playerPhotos);
                continue;
            }

            playerPhotos.sort(Comparator.comparing(PlayerCustomPhoto::getModifiedDate).reversed());
            PlayerCustomPhoto latestActivePhoto = playerPhotos.get(0);
            activePhotosAfterEnforcement.add(latestActivePhoto);

            for (int i = 1; i < playerPhotos.size(); i++) {
                PlayerCustomPhoto photo = playerPhotos.get(i);
                log.warn("Multiple active custom photo found for playerId={}, preferenceKey={} Deactivating photo.id={}",
                        playerId, preferenceKey.getKeyhash(), photo.getId());
                photo.setActive(false);
            }

            playerCustomPhotoRepository.saveAll(playerPhotos);
        }

        return activePhotosAfterEnforcement;
    }

    private void ensureSingleActivePhoto(PreferenceKey preferenceKey, long playerId) {
        List<PlayerCustomPhoto> activePhotos = playerCustomPhotoRepository.findAllActivesByPreferenceKeyAndPlayers(
                preferenceKey.getId(), Set.of(playerId));
        if (activePhotos.size() <= 1) {
            return;
        }

        activePhotos.sort(Comparator.comparing(PlayerCustomPhoto::getModifiedDate).reversed());
        for (int i = 1; i < activePhotos.size(); i++) {
            PlayerCustomPhoto photo = activePhotos.get(i);
            log.warn("Multiple active custom photos found for player={}, preferenceKey={}. Deactivating photo id={}",
                    playerId, preferenceKey.getKeyhash(), photo.getId());
            photo.setActive(false);
        }

        playerCustomPhotoRepository.saveAll(activePhotos);
    }

    private PreferenceKey getKey(String preferenceKey) {
        return preferenceKeyRepository.findByKeyhash(preferenceKey)
                .orElseThrow(() -> new IllegalArgumentException("PreferenceKey not found with key: " + preferenceKey));
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
                            log.info("Deactivate previous active photo keyhash={}, playerId={} Deactivated photo id={}", preferenceKey.getKeyhash(), playerId, photo.getId());
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