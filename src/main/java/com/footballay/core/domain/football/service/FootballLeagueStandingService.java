package com.footballay.core.domain.football.service;

import com.footballay.core.domain.football.persistence.League;
import com.footballay.core.domain.football.repository.LeagueRepository;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class FootballLeagueStandingService {

    private static final Logger log = LoggerFactory.getLogger(FootballLeagueStandingService.class);

    private final LeagueRepository leagueRepository;

    public FootballLeagueStandingService(LeagueRepository leagueRepository) {
        this.leagueRepository = leagueRepository;
    }

    @Transactional
    public void setIsStandingAvailable(Long leagueId, boolean isStandingAvailable) {
        Optional<League> findLeague = leagueRepository.findById(leagueId);
        if (findLeague.isEmpty()) {
            log.warn("League not found with id: {}", leagueId);
            return;
        }
        // findLeague.get().setStandingAvailable(isStandingAvailable);
        // log.info("League standing availability updated: {} - {}", leagueId, isStandiingAvailable);
    }

    @Transactional(readOnly = true)
    public List<League> getStandingAvailableLeagues() {
        // List<League> leagues = leagueRepository.findAllByStandingAvailableIsTrue();
        // log.info("Retrieved {} leagues with standing availability", leagues.size());
        // return leagues;
        throw new NotImplementedException("Standings API is not supported");
    }

}
