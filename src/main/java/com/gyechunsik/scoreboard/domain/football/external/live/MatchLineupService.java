package com.gyechunsik.scoreboard.domain.football.external.live;

import com.gyechunsik.scoreboard.domain.football.persistence.Fixture;
import com.gyechunsik.scoreboard.domain.football.repository.live.MatchLineupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class MatchLineupService {

    private final MatchLineupRepository matchLineupRepository;

    public boolean hasLineupData(Fixture fixture) {
        return matchLineupRepository.findAllByFixture(fixture).size() == 2;
    }

}
