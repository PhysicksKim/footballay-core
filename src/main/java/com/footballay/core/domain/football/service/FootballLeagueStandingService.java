package com.footballay.core.domain.football.service;

import com.footballay.core.domain.football.persistence.League;
import com.footballay.core.domain.football.repository.LeagueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class FootballLeagueStandingService {

    private final LeagueRepository leagueRepository;

    @Transactional
    public void setIsStandingAvailable(Long leagueId, boolean isStandingAvailable) {
        Optional<League> findLeague = leagueRepository.findById(leagueId);
        if (findLeague.isEmpty()) {
            log.warn("League not found with id: {}", leagueId);
            return;
        }
        findLeague.get().setStandingAvailable(isStandingAvailable);
        log.info("League standing availability updated: {} - {}", leagueId, isStandingAvailable);
    }

    @Transactional(readOnly = true)
    public List<League> getStandingAvailableLeagues() {
        List<League> leagues = leagueRepository.findAllByStandingAvailableIsTrue();
        log.info("Retrieved {} leagues with standing availability", leagues.size());
        return leagues;
    }

}
