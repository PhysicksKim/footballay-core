package com.gyechunsik.scoreboard.domain.football.service;

import com.gyechunsik.scoreboard.domain.football.persistence.Fixture;
import com.gyechunsik.scoreboard.domain.football.persistence.live.ExpectedGoals;
import com.gyechunsik.scoreboard.domain.football.persistence.live.FixtureEvent;
import com.gyechunsik.scoreboard.domain.football.persistence.live.LiveStatus;
import com.gyechunsik.scoreboard.domain.football.persistence.live.MatchLineup;
import com.gyechunsik.scoreboard.domain.football.persistence.live.MatchPlayer;
import com.gyechunsik.scoreboard.domain.football.persistence.live.PlayerStatistics;
import com.gyechunsik.scoreboard.domain.football.persistence.live.TeamStatistics;
import com.gyechunsik.scoreboard.domain.football.repository.FixtureRepository;
import com.gyechunsik.scoreboard.domain.football.repository.live.FixtureEventRepository;
import com.gyechunsik.scoreboard.domain.football.repository.live.MatchLineupRepository;
import com.gyechunsik.scoreboard.domain.football.repository.live.MatchPlayerRepository;
import com.gyechunsik.scoreboard.domain.football.repository.live.PlayerStatisticsRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * {@link Fixture} 에서 Available 지정 시 Job 이 등록되는데, 이때 등록된 job 으로 부터 얻어오는 각종 라이브 데이터들을 클린업 하는 데 사용됩니다. <br>
 *
 * {@link LiveStatus} 는 라이브 데이터의 성격을 띌 수 있으나, 단순한 경기결과임과 더불어 FixtureCaching 과정에서 저장되므로 삭제하지 않습니다. <br>
 * 즉 {@link LiveStatus} 는 Fixture 라이브 Job 데이터가 아니라 Fixture 결과 데이터로 간주합니다. <br>
 * @see MatchLineup
 * @see MatchPlayer
 * @see FixtureEvent
 * @see TeamStatistics
 * @see ExpectedGoals
 * @see PlayerStatistics
 */
@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class FixtureDataIntegrityService {

    private final FixtureRepository fixtureRepository;
    private final MatchLineupRepository matchLineupRepository;
    private final MatchPlayerRepository matchPlayerRepository;
    private final FixtureEventRepository fixtureEventRepository;
    private final PlayerStatisticsRepository playerStatisticsRepository;

    public void cleanUpFixtureLiveData(long fixtureId) {
        Optional<Fixture> optionalFixture = fixtureRepository.findByIdWithAllAssociations(fixtureId);
        if(optionalFixture.isEmpty()) {
            log.info("can not find fixture while cleanUpFixtureLiveData :: fixtureId={}", fixtureId);
            return;
        }

        cleanUpFixtureLiveData(optionalFixture.get());
    }

    /**
     * @param fixture Fixture 연관 데이터를 모두 사용하므로 Fetch Join 으로 load 된 Fixture 가 아니면 N+1 이 발생할 수 있습니다.
     */
    public void cleanUpFixtureLiveData(@NotNull Fixture fixture) {
        if(fixture == null) {
            log.warn("cleanUpFixtureLiveData :: fixture is null");
            return;
        }

        long fixtureId = fixture.getFixtureId();
        log.info("start cleanUpFixtureLiveData :: fixtureId={}", fixtureId);

        List<MatchLineup> lineups = fixture.getLineups();
        List<FixtureEvent> fixtureEvents = fixture.getEvents();

        removeEvents(fixtureEvents);
        removeLineups(lineups);
    }

    private void removeEvents(List<FixtureEvent> fixtureEvents) {
        if(fixtureEvents == null || fixtureEvents.isEmpty()) {
            return;
        }

        List<MatchPlayer> NotExistInLineupPlayers = new ArrayList<>();
        for (FixtureEvent fixtureEvent : fixtureEvents) {
            MatchPlayer eventPlayer = fixtureEvent.getPlayer();
            MatchPlayer eventAssist = fixtureEvent.getAssist();

            if(eventPlayer != null && eventPlayer.getMatchLineup() == null) {
                NotExistInLineupPlayers.add(eventPlayer);
            }
            if(eventAssist != null && eventAssist.getMatchLineup() == null) {
                NotExistInLineupPlayers.add(eventAssist);
            }
        }

        matchPlayerRepository.deleteAll(NotExistInLineupPlayers);
        fixtureEventRepository.deleteAll(fixtureEvents);
    }

    private void removeLineups(List<MatchLineup> lineups) {
        if(lineups == null) {
            return;
        }

        for(MatchLineup lineup : lineups) {
            removeMatchPlayerAndStatistics(lineup.getMatchPlayers());
        }
        matchLineupRepository.deleteAll(lineups);
    }

    private void removeMatchPlayerAndStatistics(List<MatchPlayer> matchPlayers) {
        if(matchPlayers.isEmpty()) {
            return;
        }

        List<PlayerStatistics> statsList = new ArrayList<>();
        for(MatchPlayer matchPlayer : matchPlayers) {
            PlayerStatistics playerStatistics = matchPlayer.getPlayerStatistics();
            if(playerStatistics != null) {
                statsList.add(playerStatistics);
            }
        }

        if(!statsList.isEmpty()) {
            playerStatisticsRepository.deleteAll(statsList);
        }
        matchPlayerRepository.deleteAll(matchPlayers);
    }

}
