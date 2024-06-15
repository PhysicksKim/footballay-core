package com.gyechunsik.scoreboard.domain.football.available;

import com.gyechunsik.scoreboard.domain.football.available.entity.AvailableLeague;
import com.gyechunsik.scoreboard.domain.football.entity.League;
import com.gyechunsik.scoreboard.domain.football.repository.FavoriteLeagueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class FootballAvailableService {

    private final FavoriteLeagueRepository favoriteLeagueRepository;

    private static final int DEFAULT_FAVORITE_LEAGUE_NUM = 10;

    public AvailableLeague addFavoriteLeague(League league) {
        log.info("addFavoriteLeague :: {}", league);

        AvailableLeague availableLeague = AvailableLeague.builder()
                .leagueId(league.getLeagueId())
                .name(league.getName())
                .build();

        return favoriteLeagueRepository.save(availableLeague);
    }

    public boolean removeFavoriteLeague(long leagueId) {
        int deletedCount = favoriteLeagueRepository.deleteByLeagueId(leagueId);
        log.info("removeFavoriteLeague :: deletedCount={}", deletedCount);
        return deletedCount > 0;
    }

    /**
     * 자주 찾는 리그 목록을 가져옵니다.
     * 등록 오래된 기준으로 10개의 리그를 가져옵니다.
     *
     * @return
     */
    public List<AvailableLeague> getFavoriteLeagues() {
        return this.getFavoriteLeagues(DEFAULT_FAVORITE_LEAGUE_NUM);
    }

    /**
     * 자주 찾는 리그 목록을 가져옵니다.
     * 등록 오래된 기준으로 num 개의 리그를 가져옵니다.
     *
     * @param num
     * @return
     */
    public List<AvailableLeague> getFavoriteLeagues(int num) {
        Page<AvailableLeague> favoriteLeagues
                = favoriteLeagueRepository.findByOrderByCreatedDateAsc(PageRequest.of(0, num));
        return favoriteLeagues.getContent();
    }

    public AvailableLeague findFavoriteLeague(long leagueId) {
        return favoriteLeagueRepository.findByLeagueId(leagueId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리그입니다."));
    }

}
