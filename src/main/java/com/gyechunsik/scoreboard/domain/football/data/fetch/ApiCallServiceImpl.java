package com.gyechunsik.scoreboard.domain.football.data.fetch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gyechunsik.scoreboard.domain.football.data.fetch.response.*;
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
            if (!response.isSuccessful()) throw new IllegalArgumentException("response fail : " + response);
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                throw new IllegalArgumentException("Response body is null for league ID " + leagueId);
            }
            return objectMapper.readValue(responseBody.string(), LeagueInfoResponse.class);
        } catch (IOException exception) {
            log.error("Api-Football call error :: leagueId={} ", leagueId, exception);
            throw new RuntimeException("Api-Football call error :: leagueId=" + leagueId, exception);
        }
    }

    // TODO : 테스트 작성필요
    @Override
    public LeagueInfoResponse teamCurrentLeaguesInfo(long teamId) {
        Request request = new Request.Builder()
                .url("https://v3.football.api-sports.io/leagues?team="+teamId+"&current=true")
                .get()
                .addHeader("X-RapidAPI-Host", "v3.football.api-sports.io")
                .addHeader("X-RapidAPI-Key", key)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IllegalArgumentException("response fail : " + response);
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                throw new IllegalArgumentException("Response body is null for league ID " + teamId);
            }
            return objectMapper.readValue(responseBody.string(), LeagueInfoResponse.class);
        } catch (IOException exception) {
            log.error("Api-Football call error :: teamId={} ", teamId, exception);
            throw new RuntimeException("Api-Football call error :: teamId=" + teamId, exception);
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
            if (!response.isSuccessful()) throw new IllegalArgumentException("response fail : " + response);
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                throw new IllegalArgumentException("Response body is null for team ID " + teamId);
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
            if (!response.isSuccessful()) throw new IllegalArgumentException("response fail : " + response);
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                throw new IllegalArgumentException("Response body is null for team ID " + teamId);
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
            if (!response.isSuccessful()) throw new IllegalArgumentException("response fail : " + response);
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                throw new IllegalArgumentException("unExpected Error when cache All Current Leagues");
            }
            return objectMapper.readValue(responseBody.string(), LeagueInfoResponse.class);
        } catch (IOException exception) {
            throw new RuntimeException("Api-Football call error :: current true call", exception);
        }
    }
}