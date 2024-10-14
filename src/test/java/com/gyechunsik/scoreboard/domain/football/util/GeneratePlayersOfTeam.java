package com.gyechunsik.scoreboard.domain.football.util;

import com.gyechunsik.scoreboard.domain.football.persistence.Player;
import com.gyechunsik.scoreboard.domain.football.persistence.Team;

import java.util.ArrayList;
import java.util.List;

public class GeneratePlayersOfTeam {

    /**
     * 주어진 팀에 속하는 선수 11명을 생성합니다.
     * @param team 영속상태의 _Team 객체
     * @return 11명의 선수 리스트
     */
    public static List<Player> generatePlayersOfTeam(Team team) {
        List<Player> players = new ArrayList<>();
        long teamId = team.getId();
        // 선수 ID 가 겹치지 않도록 하기 위하여 팀 ID 를 기준으로 100 단위로 증가시킵니다.
        // 주어진 팀이 id = 1 , id = 2 인 경우, 100을 곱해 11명을 생성하면
        // id = 1 팀은 101 ~ 112, id = 2 팀은 201 ~ 212 의 선수 ID 를 가지게 되므로 겹치지 않습니다.
        final long startId = teamId * 100L + 1;
        for(long i = startId ; i <= startId+11 ; i++) {
            String position = i==1 ? "GK" : i<=4 ? "DF" : i<=7 ? "MF" : "FW";
            players.add(
                    Player.builder()
                            .id((long) i)
                            .name("_Player" + i)
                            .koreanName("선수" + i)
                            .photoUrl("https://cdn.com/photo" + i)
                            .position(position)
                            .build()
            );
        }
        return players;
    }

}
