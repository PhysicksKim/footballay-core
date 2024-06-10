package com.gyechunsik.scoreboard.domain.football;

import com.gyechunsik.scoreboard.domain.football.entity.Fixture;
import com.gyechunsik.scoreboard.domain.football.entity.League;
import com.gyechunsik.scoreboard.domain.football.favorite.FavoriteService;
import com.gyechunsik.scoreboard.domain.football.favorite.entity.FavoriteFixture;
import com.gyechunsik.scoreboard.domain.football.favorite.entity.FavoriteLeague;
import com.gyechunsik.scoreboard.domain.football.repository.FixtureRepository;
import com.gyechunsik.scoreboard.domain.football.repository.LeagueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class FootballRoot {

    private final FavoriteService favoriteService;

    private final LeagueRepository leagueRepository;
    private final FixtureRepository fixtureRepository;

    public FavoriteLeague addFavoriteLeague(long leagueId) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리그입니다."));

        FavoriteLeague favoriteLeague = favoriteService.addFavoriteLeague(league);
        log.info("addFavoriteLeague :: {}", favoriteLeague);
        return favoriteLeague;
    }

    public FavoriteFixture addFavoriteFixture(long fixtureId) {
        Fixture fixture = fixtureRepository.findById(fixtureId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 경기입니다."));

        FavoriteFixture favoriteFixture = favoriteService.addFavoriteFixture(fixture);
        log.info("addFavoriteFixture :: {}", favoriteFixture);
        return favoriteFixture;
    }

    /**
     * 즐겨찾기 리그를 삭제합니다.
     * @param leagueId
     * @return 존재하지 않는 경우 false 를 반환합니다.
     */
    public boolean removeFavoriteLeague(long leagueId) {
        return favoriteService.removeFavoriteLeague(leagueId);
    }

    /**
     * 즐겨찾기 경기를 삭제합니다.
     * @param fixtureId
     * @return 존재하지 않는 경우 false 를 반환합니다.
     */
    public boolean removeFavoriteFixture(long fixtureId) {
        return favoriteService.removeFavoriteFixture(fixtureId);
    }

}
