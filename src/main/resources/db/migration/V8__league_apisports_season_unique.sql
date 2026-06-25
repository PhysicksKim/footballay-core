INSERT INTO league_season_core (
    league_core_id,
    season_year,
    season_start,
    season_end,
    current,
    auto_generated
)
SELECT
    provider_league.league_core_id,
    provider_season.season_year,
    MIN(provider_season.season_start),
    MAX(provider_season.season_end),
    BOOL_OR(provider_league.current_season = provider_season.season_year),
    FALSE
FROM league_apisports_season provider_season
JOIN league_apisports provider_league
    ON provider_league.id = provider_season.league_apisports_id
WHERE provider_league.league_core_id IS NOT NULL
  AND provider_season.season_year IS NOT NULL
GROUP BY provider_league.league_core_id, provider_season.season_year
ON CONFLICT (league_core_id, season_year) DO NOTHING;

UPDATE league_apisports_season provider_season
SET league_season_core_id = core_season.id
FROM league_apisports provider_league
JOIN league_season_core core_season
    ON core_season.league_core_id = provider_league.league_core_id
WHERE provider_season.league_apisports_id = provider_league.id
  AND core_season.season_year = provider_season.season_year
  AND provider_season.league_season_core_id IS NULL;

WITH duplicate_seasons AS (
    SELECT
        id,
        FIRST_VALUE(id) OVER (
            PARTITION BY league_apisports_id, season_year
            ORDER BY (league_season_core_id IS NOT NULL) DESC, id ASC
        ) AS keep_id
    FROM league_apisports_season
    WHERE league_apisports_id IS NOT NULL
      AND season_year IS NOT NULL
),
season_rewrites AS (
    SELECT id, keep_id
    FROM duplicate_seasons
    WHERE id <> keep_id
)
UPDATE fixture_api_sports fixture
SET season_id = season_rewrites.keep_id
FROM season_rewrites
WHERE fixture.season_id = season_rewrites.id;

WITH duplicate_seasons AS (
    SELECT
        id,
        ROW_NUMBER() OVER (
            PARTITION BY league_apisports_id, season_year
            ORDER BY (league_season_core_id IS NOT NULL) DESC, id ASC
        ) AS row_number
    FROM league_apisports_season
    WHERE league_apisports_id IS NOT NULL
      AND season_year IS NOT NULL
)
DELETE FROM league_apisports_season season
USING duplicate_seasons
WHERE season.id = duplicate_seasons.id
  AND duplicate_seasons.row_number > 1;

ALTER TABLE league_apisports_season
    ADD CONSTRAINT uc_league_apisports_season_league_year UNIQUE (league_apisports_id, season_year);
