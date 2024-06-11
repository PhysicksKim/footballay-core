package com.gyechunsik.scoreboard.domain.football.service;

import com.gyechunsik.scoreboard.domain.football.entity.League;
import com.gyechunsik.scoreboard.domain.football.repository.LeagueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class FootballDataService {

    private final LeagueRepository leagueRepository;

    public League getLeagueById(long leagueId) {
        return leagueRepository.findById(leagueId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리그입니다."));
    }
}
