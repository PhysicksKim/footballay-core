ALTER TABLE league_apisports_season
    ADD CONSTRAINT uc_league_apisports_season_core UNIQUE (league_season_core_id);
