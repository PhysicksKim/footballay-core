package com.gyechunsik.scoreboard.domain.football.external.fetch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gyechunsik.scoreboard.domain.football.external.fetch.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
@Profile("api")
@Service
public class ApiCallServiceImpl implements ApiCallService {

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper objectMapper;

    @Value("${rapidapi.football.key}")
    private String key;

    @Override
    public ExternalApiStatusResponse status() {
        Request request = new Request.Builder()
                .url("https://v3.football.api-sports.io/status")
                .get()
                .addHeader("X-RapidAPI-Host", "v3.football.api-sports.io")
                .addHeader("X-RapidAPI-Key", key)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful())
                throw new IllegalArgumentException("response fail : " + response);
            ResponseBody responseBody = response.body();
            Headers headers = response.headers();

            if (responseBody == null) {
                throw new IllegalArgumentException("Api Status body is null");
            }
            return mapToStatusResponse(responseBody, headers);
        } catch (IOException exception) {
            log.error("Api-Football call error :: api status ", exception);
            throw new RuntimeException("Api-Football call error :: api status", exception);
        }
    }

    @Override
    public LeagueInfoResponse leagueInfo(long leagueId) {
        Request request = new Request.Builder()
                .url("https://v3.football.api-sports.io/leagues?id=" + leagueId + "&current=true")
                .get()
                .addHeader("X-RapidAPI-Host", "v3.football.api-sports.io")
                .addHeader("X-RapidAPI-Key", key)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful())
                throw new IllegalArgumentException("response fail : " + response);
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                throw new IllegalArgumentException("_FixtureSingle body is null for league ID " + leagueId);
            }
            return objectMapper.readValue(responseBody.string(), LeagueInfoResponse.class);
        } catch (IOException exception) {
            log.error("Api-Football call error :: leagueId={} ", leagueId, exception);
            throw new RuntimeException("Api-Football call error :: leagueId=" + leagueId, exception);
        }
    }

    @Override
    public LeagueInfoResponse teamCurrentLeaguesInfo(long teamId) {
        Request request = new Request.Builder()
                .url("https://v3.football.api-sports.io/leagues?team=" + teamId + "&current=true")
                .get()
                .addHeader("X-RapidAPI-Host", "v3.football.api-sports.io")
                .addHeader("X-RapidAPI-Key", key)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful())
                throw new IllegalArgumentException("response fail : " + response);
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                throw new IllegalArgumentException("_FixtureSingle body is null for league ID " + teamId);
            }
            return objectMapper.readValue(responseBody.string(), LeagueInfoResponse.class);
        } catch (IOException exception) {
            log.error("Api-Football call error :: teamId={} ", teamId, exception);
            throw new RuntimeException("Api-Football call error :: teamId=" + teamId, exception);
        }
    }

    @Override
    public TeamInfoResponse teamsInfo(long leagueId, int currentSeason) {
        Request request = new Request.Builder()
                .url("https://v3.football.api-sports.io/teams?league=" + leagueId + "&season=" + currentSeason)
                .get()
                .addHeader("X-RapidAPI-Host", "v3.football.api-sports.io")
                .addHeader("X-RapidAPI-Key", key)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful())
                throw new IllegalArgumentException("response fail : " + response);
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                throw new IllegalArgumentException("_FixtureSingle body is null for league ID " + leagueId);
            }
            return objectMapper.readValue(responseBody.string(), TeamInfoResponse.class);
        } catch (IOException exception) {
            log.error("Api-Football call error :: leagueId={} ", leagueId, exception);
            throw new RuntimeException("Api-Football call error :: leagueId=" + leagueId, exception);
        }
    }

    @Override
    public TeamInfoResponse teamInfo(long teamId) {
        Request request = new Request.Builder()
                .url("https://v3.football.api-sports.io/teams?id=" + teamId)
                .get()
                .addHeader("X-RapidAPI-Host", "v3.football.api-sports.io")
                .addHeader("X-RapidAPI-Key", key)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful())
                throw new IllegalArgumentException("response fail : " + response);
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                throw new IllegalArgumentException("_FixtureSingle body is null for team ID " + teamId);
            }
            return objectMapper.readValue(responseBody.string(), TeamInfoResponse.class);
        } catch (IOException exception) {
            log.error("Api-Football call error :: teamId={} ", teamId, exception);
            throw new RuntimeException("Api-Football call error :: teamId=" + teamId, exception);
        }
    }

    @Override
    public PlayerSquadResponse playerSquad(long teamId) {
        Request request = new Request.Builder()
                .url("https://v3.football.api-sports.io/players/squads?team=" + teamId)
                .get()
                .addHeader("X-RapidAPI-Host", "v3.football.api-sports.io")
                .addHeader("X-RapidAPI-Key", key)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful())
                throw new IllegalArgumentException("response fail : " + response);
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                throw new IllegalArgumentException("_FixtureSingle body is null for team ID " + teamId);
            }
            return objectMapper.readValue(responseBody.string(), PlayerSquadResponse.class);
        } catch (IOException exception) {
            log.error("Api-Football call error :: teamId={} ", teamId, exception);
            throw new RuntimeException("Api-Football call error :: teamId=" + teamId, exception);
        }
    }

    @Override
    public LeagueInfoResponse allLeagueCurrent() {
        Request request = new Request.Builder()
                .url("https://v3.football.api-sports.io/leagues?current=true")
                .get()
                .addHeader("X-RapidAPI-Host", "v3.football.api-sports.io")
                .addHeader("X-RapidAPI-Key", key)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful())
                throw new IllegalArgumentException("response fail : " + response);
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                throw new IllegalArgumentException("unExpected Error when cache All Current Leagues");
            }
            return objectMapper.readValue(responseBody.string(), LeagueInfoResponse.class);
        } catch (IOException exception) {
            throw new RuntimeException("Api-Football call error :: current true call", exception);
        }
    }

    // example request
    // _FixtureSingle | GET : https://v3.football.api-sports.io/fixtures?league=4&season=2024
    @Override
    public FixtureResponse fixturesOfLeagueSeason(long leagueId, int season) {
        Request request = new Request.Builder()
                .url("https://v3.football.api-sports.io/fixtures?league=" + leagueId + "&season=" + season)
                .get()
                .addHeader("X-RapidAPI-Host", "v3.football.api-sports.io")
                .addHeader("X-RapidAPI-Key", key)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful())
                throw new IllegalArgumentException("response fail : " + response);
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                throw new IllegalArgumentException("_FixtureSingle body is null for league ID " + leagueId);
            }
            return objectMapper.readValue(responseBody.string(), FixtureResponse.class);
        } catch (IOException exception) {
            log.error("Api-Football call error :: leagueId={} ", leagueId, exception);
            throw new RuntimeException("Api-Football call error :: leagueId=" + leagueId, exception);
        }
    }

    @Override
    public FixtureSingleResponse fixtureSingle(long fixtureId) {
        Request request = new Request.Builder()
                .url("https://v3.football.api-sports.io/fixtures?id=" + fixtureId)
                .get()
                .addHeader("X-RapidAPI-Host", "v3.football.api-sports.io")
                .addHeader("X-RapidAPI-Key", key)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful())
                throw new IllegalArgumentException("response fail : " + response);
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                throw new IllegalArgumentException("_FixtureSingle body is null. fixture ID : " + fixtureId);
            }
            FixtureSingleResponse fixtureSingleResponse = objectMapper.readValue(responseBody.string(), FixtureSingleResponse.class);

            // DEBUG for 2024-11-11 EPL 11R chelsea vs arsenal
            final boolean DEBUG_UNREGI_PLAYER = false;
            if(fixtureId == 1208125 && DEBUG_UNREGI_PLAYER) {
                // Modify the player ID if it matches the target ID
                final long chelseaJacksonId = 283058L; // Jackson player's original ID

                // Call helper methods to update the player ID
                updateEventPlayerIdToNull(fixtureSingleResponse, chelseaJacksonId);
                updateLineupPlayerIdToNull(fixtureSingleResponse, chelseaJacksonId);
                updatePlayerStatisticsIdToNull(fixtureSingleResponse, chelseaJacksonId);
            }

            return fixtureSingleResponse;
        } catch (IOException exception) {
            log.error("Api-Football call error :: fixtureId={} ", fixtureId, exception);
            throw new RuntimeException("Api-Football call error :: fixtureId=" + fixtureId, exception);
        }
    }

    // Response | GET : https://v3.football.api-sports.io/players?id=629&league=39&season=2024
    @Override
    public PlayerInfoResponse playerSingle(long playerId, long leagueId, int season) {
        Request request = new Request.Builder()
                .url("https://v3.football.api-sports.io/players?id=" + playerId + "&league=" + leagueId + "&season=" + season)
                .get()
                .addHeader("X-RapidAPI-Host", "v3.football.api-sports.io")
                .addHeader("X-RapidAPI-Key", key)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful())
                throw new IllegalArgumentException("response fail : " + response);
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                throw new IllegalArgumentException("player single body is null for player ID " + playerId);
            }
            return objectMapper.readValue(responseBody.string(), PlayerInfoResponse.class);
        } catch (IOException exception) {
            log.error("Api-Football call error :: playerId={},leagueId={},season={} ", playerId, leagueId, season, exception);
            throw new RuntimeException("Api-Football call error :: playerId=" + playerId + ",leagueId="+leagueId+",season="+season, exception);
        }
    }

    // Helper method to update player ID in events
    private void updateEventPlayerIdToNull(FixtureSingleResponse fixtureSingleResponse, long targetUnregisteredPlayerId) {
        if (fixtureSingleResponse.getResponse() != null) {
            for (FixtureSingleResponse._FixtureSingle fixtureSingle : fixtureSingleResponse.getResponse()) {
                if (fixtureSingle.getEvents() != null) {
                    for (FixtureSingleResponse._Events event : fixtureSingle.getEvents()) {
                        if (event.getPlayer() != null && event.getPlayer().getId() != null
                                && event.getPlayer().getId().equals(targetUnregisteredPlayerId)) {
                            event.getPlayer().setId(null);
                        }
                        if (event.getAssist() != null && event.getAssist().getId() != null
                                && event.getAssist().getId().equals(targetUnregisteredPlayerId)) {
                            event.getPlayer().setId(null);
                        }
                    }
                }
            }
        }
    }

    // Helper method to update player ID in lineups
    private void updateLineupPlayerIdToNull(FixtureSingleResponse fixtureSingleResponse, long targetUnregisteredPlayerId) {
        if (fixtureSingleResponse.getResponse() != null) {
            for (FixtureSingleResponse._FixtureSingle fixtureSingle : fixtureSingleResponse.getResponse()) {
                if (fixtureSingle.getLineups() != null) {
                    for (FixtureSingleResponse._Lineups lineup : fixtureSingle.getLineups()) {
                        if (lineup.getStartXI() != null) {
                            for (FixtureSingleResponse._Lineups._StartPlayer player : lineup.getStartXI()) {
                                if (player.getPlayer() != null && player.getPlayer().getId() != null
                                        && player.getPlayer().getId().equals(targetUnregisteredPlayerId)) {
                                    player.getPlayer().setId(null); // Set the ID to null
                                }
                            }
                        }
                        if (lineup.getSubstitutes() != null) {
                            for (FixtureSingleResponse._Lineups._StartPlayer player : lineup.getSubstitutes()) {
                                if (player.getPlayer() != null && player.getPlayer().getId() != null
                                        && player.getPlayer().getId().equals(targetUnregisteredPlayerId)) {
                                    player.getPlayer().setId(null); // Set the ID to null
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Helper method to update player ID in playerStatistics
    private void updatePlayerStatisticsIdToNull(FixtureSingleResponse fixtureSingleResponse, long targetUnregisteredPlayerId) {
        if (fixtureSingleResponse.getResponse() != null) {
            for (FixtureSingleResponse._FixtureSingle fixtureSingle : fixtureSingleResponse.getResponse()) {
                if (fixtureSingle.getPlayers() != null) {
                    for (FixtureSingleResponse._FixturePlayers fixturePlayers : fixtureSingle.getPlayers()) {
                        if (fixturePlayers.getPlayers() != null) {
                            for (FixtureSingleResponse._FixturePlayers._PlayerStatistics playerStatistics : fixturePlayers.getPlayers()) {
                                FixtureSingleResponse._FixturePlayers._Player player = playerStatistics.getPlayer();
                                if (player != null && player.getId() != null && player.getId().equals(targetUnregisteredPlayerId)) {
                                    player.setId(null); // Set the ID to null
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private @NotNull ExternalApiStatusResponse mapToStatusResponse(ResponseBody responseBody, Headers headers) throws IOException {
        ExternalApiStatusResponse mappedResponse = objectMapper.readValue(responseBody.string(), ExternalApiStatusResponse.class);
        ExternalApiStatusResponse._Headers mappedHeaders = new ExternalApiStatusResponse._Headers();
        mappedHeaders.setXRatelimitLimit(parseIntOrMinusOne(headers.get("X-Ratelimit-Limit")));
        mappedHeaders.setXRatelimitRemaining(parseIntOrMinusOne(headers.get("X-Ratelimit-Remaining")));
        mappedHeaders.setXRatelimitRequestsLimit(parseIntOrMinusOne(headers.get("X-Ratelimit-Requests-Limit")));
        mappedHeaders.setXRatelimitRequestsRemaining(parseIntOrMinusOne(headers.get("X-Ratelimit-Requests-Remaining")));
        mappedResponse.setHeaders(mappedHeaders);
        return mappedResponse;
    }

    private int parseIntOrMinusOne(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

}