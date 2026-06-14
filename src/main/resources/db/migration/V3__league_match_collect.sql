ALTER TABLE league_core
    ADD COLUMN match_collect VARCHAR(32) NOT NULL DEFAULT 'NONE';

CREATE INDEX idx_league_core_match_collect
    ON league_core (match_collect);

CREATE INDEX idx_league_core_available_match_collect
    ON league_core (available, match_collect);
