package com.gyechunsik.scoreboard.web.controller.admin;

import com.gyechunsik.scoreboard.domain.football.external.ExternalApiCacheFacade;
import com.gyechunsik.scoreboard.domain.football.favorite.entity.FavoriteLeague;
import com.gyechunsik.scoreboard.domain.football.repository.FavoriteLeagueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminRestController {

    private final ExternalApiCacheFacade externalApiCacheFacade;
    private final FavoriteLeagueRepository favoriteLeagueRepository;

    @GetMapping("/league/available")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> availableLeagues() {
        return ResponseEntity.ok("This is available leagues response for test");
    }

    @GetMapping("/league/favorite")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String,Object>> getFavoriteLeagues() {
        List<FavoriteLeague> all = favoriteLeagueRepository.findAll();
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("data", all,"count", all.size()));
    }

}
