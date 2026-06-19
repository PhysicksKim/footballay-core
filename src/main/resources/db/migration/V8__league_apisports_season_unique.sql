ALTER TABLE league_apisports_season
    ADD CONSTRAINT uc_league_apisports_season_league_year UNIQUE (league_apisports_id, season_year);
