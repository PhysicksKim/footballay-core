package com.gyechunsik.scoreboard.domain.football.favorite;

import com.gyechunsik.scoreboard.domain.football.entity.League;
import com.gyechunsik.scoreboard.domain.football.favorite.entity.FavoriteLeague;
import com.gyechunsik.scoreboard.domain.football.repository.FavoriteLeagueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class FavoriteService {

    private final FavoriteLeagueRepository favoriteLeagueRepository;

    private static final int DEFAULT_FAVORITE_LEAGUE_NUM = 10;

    public FavoriteLeague addFavoriteLeague(League league) {
        log.info("addFavoriteLeague :: {}", league);

        FavoriteLeague favoriteLeague = FavoriteLeague.builder()
                .leagueId(league.getLeagueId())
                .season(league.getCurrentSeason())
                .name(league.getName())
                .koreanName(league.getKoreanName())
                .build();

        return favoriteLeagueRepository.save(favoriteLeague);
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
    public List<FavoriteLeague> getFavoriteLeagues() {
        return this.getFavoriteLeagues(DEFAULT_FAVORITE_LEAGUE_NUM);
    }

    /**
     * 자주 찾는 리그 목록을 가져옵니다.
     * 등록 오래된 기준으로 num 개의 리그를 가져옵니다.
     *
     * @param num
     * @return
     */
    public List<FavoriteLeague> getFavoriteLeagues(int num) {
        Page<FavoriteLeague> favoriteLeagues
                = favoriteLeagueRepository.findByOrderByCreatedDateAsc(PageRequest.of(0, num));
        return favoriteLeagues.getContent();
    }

    public FavoriteLeague findFavoriteLeague(long leagueId) {
        return favoriteLeagueRepository.findByLeagueId(leagueId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리그입니다."));
    }

}
