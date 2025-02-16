package com.gyechunsik.scoreboard.domain.football.persistence.standings;

import jakarta.persistence.*;

@Embeddable
public class MatchStats {

    private int played;

    private int win;

    private int draw;

    private int lose;

    @Column(name = "goals_for")
    private int goalsFor;

    @Column(name = "goals_against")
    private int goalsAgainst;

}
