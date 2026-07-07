package com.footballay.core.web.admin.football.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/football")
@PreAuthorize("hasRole(\'ADMIN\')")
public class AdminFootballCustomPhotoController {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AdminFootballCustomPhotoController.class);
    private static final String DISABLED_HEADER = "X-Footballay-Feature-Status";
    private static final String DISABLED_FEATURE = "custom-photo-disabled";
    private static final String DISABLED_MESSAGE = "Custom photo feature is disabled.";

    @PostMapping("/preference")
    public ResponseEntity<?> createPreferenceKey(Authentication auth) {
        return customPhotoUnsupported();
    }

    @PatchMapping("/preference")
    public ResponseEntity<?> reissuePreferenceKey(Authentication auth) {
        return customPhotoUnsupported();
    }

    @GetMapping("/teams/{teamId}/squad/custom")
    public ResponseEntity<?> getSquadWithCustomPhotos(Authentication auth, @PathVariable long teamId) {
        return customPhotoUnsupported();
    }

    @GetMapping("/players/{playerId}/photos")
    public ResponseEntity<?> getPlayerRegisteredPhotos(Authentication auth, @PathVariable long playerId) {
        return customPhotoUnsupported();
    }

    @PostMapping("/players/{playerId}/photos")
    public ResponseEntity<?> uploadPlayerPhoto(Authentication auth, @PathVariable long playerId, @RequestPart MultipartFile photo) {
        return customPhotoUnsupported();
    }

    @PatchMapping("/photos/{photoId}/activate")
    public ResponseEntity<?> activatePhoto(Authentication auth, @PathVariable long photoId) {
        return customPhotoUnsupported();
    }

    @PatchMapping("/photos/{photoId}/deactivate")
    public ResponseEntity<?> deactivatePhoto(Authentication auth, @PathVariable long photoId) {
        return customPhotoUnsupported();
    }

    @PatchMapping("/players/{playerId}/photos/default")
    public ResponseEntity<?> useDefaultProfilePhoto(Authentication auth, @PathVariable long playerId) {
        return customPhotoUnsupported();
    }

    @DeleteMapping("/photos/{photoId}")
    public ResponseEntity<?> deletePhoto(Authentication auth, @PathVariable long photoId) {
        return customPhotoUnsupported();
    }

    private ResponseEntity<String> customPhotoUnsupported() {
        log.info("Custom photo endpoint requested but feature is disabled.");
        return ResponseEntity
                .status(HttpStatus.NOT_IMPLEMENTED)
                .header(DISABLED_HEADER, DISABLED_FEATURE)
                .body(DISABLED_MESSAGE);
    }
}
