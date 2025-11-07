package com.footballay.core.domain.football.persistence.standings;

import jakarta.persistence.*;

@Embeddable
public class StandingStats {

    private int played;

    private int win;

    private int draw;

    private int lose;

    @Column(name = "goals_for")
    private int goalsFor;

    @Column(name = "goals_against")
    private int goalsAgainst;

    // No-args constructor
    public StandingStats() {
    }

    // All-args constructor
    public StandingStats(int played, int win, int draw, int lose, int goalsFor, int goalsAgainst) {
        this.played = played;
        this.win = win;
        this.draw = draw;
        this.lose = lose;
        this.goalsFor = goalsFor;
        this.goalsAgainst = goalsAgainst;
    }

    // Getters
    public int getPlayed() {
        return played;
    }

    public int getWin() {
        return win;
    }

    public int getDraw() {
        return draw;
    }

    public int getLose() {
        return lose;
    }

    public int getGoalsFor() {
        return goalsFor;
    }

    public int getGoalsAgainst() {
        return goalsAgainst;
    }

    // Setters
    public void setPlayed(int played) {
        this.played = played;
    }

    public void setWin(int win) {
        this.win = win;
    }

    public void setDraw(int draw) {
        this.draw = draw;
    }

    public void setLose(int lose) {
        this.lose = lose;
    }

    public void setGoalsFor(int goalsFor) {
        this.goalsFor = goalsFor;
    }

    public void setGoalsAgainst(int goalsAgainst) {
        this.goalsAgainst = goalsAgainst;
    }

    // Builder
    public static StandingStatsBuilder builder() {
        return new StandingStatsBuilder();
    }

    public static class StandingStatsBuilder {
        private int played;
        private int win;
        private int draw;
        private int lose;
        private int goalsFor;
        private int goalsAgainst;

        StandingStatsBuilder() {
        }

        public StandingStatsBuilder played(int played) {
            this.played = played;
            return this;
        }

        public StandingStatsBuilder win(int win) {
            this.win = win;
            return this;
        }

        public StandingStatsBuilder draw(int draw) {
            this.draw = draw;
            return this;
        }

        public StandingStatsBuilder lose(int lose) {
            this.lose = lose;
            return this;
        }

        public StandingStatsBuilder goalsFor(int goalsFor) {
            this.goalsFor = goalsFor;
            return this;
        }

        public StandingStatsBuilder goalsAgainst(int goalsAgainst) {
            this.goalsAgainst = goalsAgainst;
            return this;
        }

        public StandingStats build() {
            return new StandingStats(played, win, draw, lose, goalsFor, goalsAgainst);
        }
    }
}
