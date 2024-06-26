package com.gyechunsik.scoreboard.domain.football.external.fetch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gyechunsik.scoreboard.domain.football.external.fetch.response.*;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Profile("api")
@Service
public class ApiCallServiceImpl implements ApiCallService {

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${rapidapi.football.key}")
    private String key;

    @Override
    public LeagueInfoResponse leagueInfo(long leagueId) {
        Request request = new Request.Builder()
                .url("https://v3.football.api-sports.io/leagues?id="+leagueId+"&current=true")
                .get()
                .addHeader("X-RapidAPI-Host", "v3.football.api-sports.io")
                .addHeader("X-RapidAPI-Key", key)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful())
                throw new IllegalArgumentException("response fail : " + response);
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                throw new IllegalArgumentException("FixtureSingle body is null for league ID " + leagueId);
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
                .url("https://v3.football.api-sports.io/leagues?team="+teamId+"&current=true")
                .get()
                .addHeader("X-RapidAPI-Host", "v3.football.api-sports.io")
                .addHeader("X-RapidAPI-Key", key)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful())
                throw new IllegalArgumentException("response fail : " + response);
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                throw new IllegalArgumentException("FixtureSingle body is null for league ID " + teamId);
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
                .url("https://v3.football.api-sports.io/teams?league="+leagueId+"&season="+currentSeason)
                .get()
                .addHeader("X-RapidAPI-Host", "v3.football.api-sports.io")
                .addHeader("X-RapidAPI-Key", key)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful())
                throw new IllegalArgumentException("response fail : " + response);
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                throw new IllegalArgumentException("FixtureSingle body is null for league ID " + leagueId);
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
                throw new IllegalArgumentException("FixtureSingle body is null for team ID " + teamId);
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
                throw new IllegalArgumentException("FixtureSingle body is null for team ID " + teamId);
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
    // FixtureSingle | GET : https://v3.football.api-sports.io/fixtures?league=4&season=2024
    @Override
    public FixtureResponse fixturesOfLeagueSeason(long leagueId, int season) {
        Request request = new Request.Builder()
                .url("https://v3.football.api-sports.io/fixtures?league="+leagueId+"&season="+season)
                .get()
                .addHeader("X-RapidAPI-Host", "v3.football.api-sports.io")
                .addHeader("X-RapidAPI-Key", key)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful())
                throw new IllegalArgumentException("response fail : " + response);
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                throw new IllegalArgumentException("FixtureSingle body is null for league ID " + leagueId);
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
                .url("https://v3.football.api-sports.io/fixtures?id="+fixtureId)
                .get()
                .addHeader("X-RapidAPI-Host", "v3.football.api-sports.io")
                .addHeader("X-RapidAPI-Key", key)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful())
                throw new IllegalArgumentException("response fail : " + response);
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                throw new IllegalArgumentException("FixtureSingle body is null. fixture ID : " + fixtureId);
            }
            return objectMapper.readValue(responseBody.string(), FixtureSingleResponse.class);
        } catch (IOException exception) {
            log.error("Api-Football call error :: fixtureId={} ", fixtureId, exception);
            throw new RuntimeException("Api-Football call error :: fixtureId=" + fixtureId, exception);
        }
    }

}