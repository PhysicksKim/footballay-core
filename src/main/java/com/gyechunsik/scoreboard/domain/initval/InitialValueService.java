package com.gyechunsik.scoreboard.domain.initval;

import com.gyechunsik.scoreboard.domain.initval.Entity.DefaultMatch;
import com.gyechunsik.scoreboard.domain.initval.Entity.DefaultTeam;
import com.gyechunsik.scoreboard.domain.initval.Entity.Streamer;
import com.gyechunsik.scoreboard.domain.initval.Entity.enums.DefaultTeamCodes;
import com.gyechunsik.scoreboard.domain.initval.Entity.enums.DefaultUniform;
import com.gyechunsik.scoreboard.domain.initval.Entity.enums.LeagueCategory;
import com.gyechunsik.scoreboard.domain.initval.Entity.enums.TeamSide;
import com.gyechunsik.scoreboard.domain.initval.repository.DefaultMatchRepository;
import com.gyechunsik.scoreboard.domain.initval.repository.DefaultTeamRepository;
import com.gyechunsik.scoreboard.domain.initval.repository.StreamerRepository;
import com.gyechunsik.scoreboard.web.response.DefaultMatchResponse;
import com.gyechunsik.scoreboard.web.response.DefaultTeamResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class InitialValueService {

    private final StreamerRepository streamerRepository;
    private final DefaultMatchRepository defaultMatchRepository;
    private final DefaultTeamRepository defaultTeamRepository;

    public Map<String, Object> getInitialValueJson(String streamerHash) {
        Streamer streamer = streamerRepository.findByHash(streamerHash).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 streamerHash 입니다. hash=" + streamerHash));
        DefaultMatch defaultMatch = defaultMatchRepository.findDefaultMatchByStreamer(streamer).orElseThrow(() -> new IllegalArgumentException("Streamer Hash 와 일치하는 defaultMatch 를 찾을 수 없습니다."));
        List<DefaultTeam> defaultTeams = defaultTeamRepository.findDefaultTeamsByStreamer(streamer);
        if (defaultTeams.size() != 2)
            throw new IllegalArgumentException("Streamer Hash 와 일치하는 defaultTeam 찾은 결과가 2개가 아닙니다. size=" + defaultTeams.size());

        DefaultTeam teamA = null, teamB = null;
        for (DefaultTeam defaultTeam : defaultTeams) {
            if (defaultTeam.getSide() == TeamSide.A) {
                teamA = defaultTeam;
            } else if (defaultTeam.getSide() == TeamSide.B) {
                teamB = defaultTeam;
            }
        }
        if (teamA == null || teamB == null) {
            throw new IllegalArgumentException("찾은 DefaultTeams 의 Side 무결성에 문제가 있습니다. " + defaultTeams);
        }

        log.info("Find Result : StreamerHash={}", streamerHash);
        log.info("Find Result : Match={}", defaultMatch);
        log.info("Find Result : TeamA={}", teamA);
        log.info("Find Result : TeamB={}", teamB);

        Map<String, Object> result = new HashMap<>();
        Map<String, String> matchMap = Map.of("name", defaultMatch.getName());
        Map<String, String> teamAMap = Map.of("category", teamA.getCategory().name(), "code", teamA.getCode().name(), "uniform", teamA.getUniform().name(), "side", teamA.getSide().name());
        Map<String, String> teamBMap = Map.of("category", teamB.getCategory().name(), "code", teamB.getCode().name(), "uniform", teamB.getUniform().name(), "side", teamB.getSide().name());

        log.info("Match : {}", matchMap);
        log.info("teamAMap : {}", teamAMap);
        log.info("teamBMap : {}", teamBMap);

        result.put("match", matchMap);
        result.put("teamA", teamAMap);
        result.put("teamB", teamBMap);

        return result;
    }

    public Streamer createStreamer(String streamerName) {
        Streamer savedStreamer;
        try {
            savedStreamer = streamerRepository.save(new Streamer(streamerName));
        } catch (DataIntegrityViolationException exception) {
            log.info("중복된 이름으로 스트리머 생성을 요청 했습니다. streamerName={}", streamerName);
            throw new IllegalArgumentException("중복된 이름으로 스트리머 생성을 요청 했습니다. streamerName=" + streamerName, exception);
        }

        log.info("New Streamer Saved! streamer={}", savedStreamer);
        return savedStreamer;
    }

    public Streamer findStreamer(String streamerName) {
        Optional<Streamer> optionalStreamer = streamerRepository.findByName(streamerName);
        return optionalStreamer.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 스트리머 이름입니다. streamerName=" + streamerName));
    }

    /**
     * 기존에 존재하는
     *
     * @param streamerHash
     * @param matchName
     * @return
     */
    public DefaultMatch saveDefaultMatch(String streamerHash, String matchName) {
        Streamer streamer = streamerRepository.findByHash(streamerHash)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 streamerHash 입니다. hash=" + streamerHash));

        Optional<DefaultMatch> optionalDefaultMatch = defaultMatchRepository.findDefaultMatchByStreamer(streamer);
        DefaultMatch defaultMatch;
        if (optionalDefaultMatch.isPresent()) {
            defaultMatch = optionalDefaultMatch.get();
            defaultMatch.setName(matchName);
        } else {
            defaultMatch = new DefaultMatch(matchName, streamer);
        }
        return defaultMatchRepository.save(defaultMatch);
    }

    public DefaultMatchResponse findDefaultMatch(String streamerHash) {
        DefaultMatch defaultMatch =
                defaultMatchRepository.findDefaultMatchByStreamer(
                                streamerRepository.findByHash(streamerHash)
                                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 streamerHash 입니다. hash=" + streamerHash)))
                        .orElseThrow(() -> new IllegalArgumentException("Streamer Hash 와 일치하는 defaultMatch 를 찾을 수 없습니다."));

        return new DefaultMatchResponse(defaultMatch.getName());
    }

    public DefaultTeamResponse saveTeam(String streamerHash, LeagueCategory category, DefaultTeamCodes teamCode, TeamSide side, DefaultUniform uniform) {
        Streamer streamer = streamerRepository.findByHash(streamerHash)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 streamerHash 입니다. hash=" + streamerHash));
        Optional<DefaultTeam> optionalDefaultTeamA = defaultTeamRepository.findByStreamerAndSide(streamer, side);
        DefaultTeam defaultTeam;
        if (optionalDefaultTeamA.isPresent()) {
            log.info("default team found by streamer hash exist");
            log.info("default team : {}", optionalDefaultTeamA.get());
            defaultTeam = optionalDefaultTeamA.get();
            defaultTeam.setSide(side);
            defaultTeam.setCategory(category);
            defaultTeam.setCode(teamCode);
            defaultTeam.setUniform(uniform);
        } else {
            log.info("default team not found by streamer hash exist");
            defaultTeam = new DefaultTeam(side, category, teamCode, uniform, streamer);
            log.info("create new default team :: {}", defaultTeam);
        }

        DefaultTeamResponse response = new DefaultTeamResponse(
                defaultTeam.getCategory().name(),
                defaultTeam.getCode().name(),
                defaultTeam.getCode().getKoreaName(),
                defaultTeam.getSide().name(),
                defaultTeam.getUniform().name()
        );
        return response;
    }

    /**
     * @param streamerHash
     * @return length=2 배열 {teamA, teamB} 를 반환합니다.
     */
    public DefaultTeamResponse[] findTeam(String streamerHash) {
        Streamer streamer = streamerRepository.findByHash(streamerHash)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 streamerHash 입니다. hash=" + streamerHash));

        List<DefaultTeam> teams = defaultTeamRepository.findByStreamer(streamer);

        if (teams.size() != 2) {
            throw new IllegalArgumentException("Streamer Hash 와 일치하는 defaultTeam 찾은 결과가 2개가 아닙니다. Team은 Side A 와 B 로 size=2 이어야 합니다. find list size=" + teams.size());
        }

        DefaultTeam teamA = null, teamB = null;
        for (DefaultTeam team : teams) {
            if (team.getSide() == TeamSide.A) {
                teamA = team;
            } else if (team.getSide() == TeamSide.B) {
                teamB = team;
            }
        }
        if (teamA == null || teamB == null) {
            throw new IllegalArgumentException("Side 무결성에 문제가 있습니다. " + teams);
        }

        DefaultTeamResponse responseA = new DefaultTeamResponse(
                teamA.getCategory().name(),
                teamA.getCode().name(),
                teamA.getCode().getKoreaName(),
                teamA.getSide().name(),
                teamA.getUniform().name()
        );
        DefaultTeamResponse responseB = new DefaultTeamResponse(
                teamB.getCategory().name(),
                teamB.getCode().name(),
                teamB.getCode().getKoreaName(),
                teamB.getSide().name(),
                teamB.getUniform().name()
        );

        return new DefaultTeamResponse[]{responseA, responseB};
    }

    public Map<String, String> getLeagueCategories() {
        return Arrays.stream(LeagueCategory.values())
                .collect(
                        HashMap::new,
                        (map, category) ->
                                map.put(category.name(), category.getValue()),
                        HashMap::putAll
                );
    }

    public Map<String, String> getTeamCodes(LeagueCategory category) {
        return Arrays.stream(DefaultTeamCodes.values())
                .filter(t -> t.getCategory().equals(category.getValue()))
                .collect(
                        HashMap::new,
                        (map, teamCode) ->
                                map.put(teamCode.name(), teamCode.getName()),
                        HashMap::putAll
                );
    }

    public List<String> getUniforms() {
        return Arrays.stream(DefaultUniform.values()).map(Enum::name).toList();
    }

    public Map<String, String> getStreamers() {
        return streamerRepository.findAll().stream().collect(
                HashMap::new,
                ((hashMap, streamer) -> hashMap.put(streamer.getName(), streamer.getHash())),
                HashMap::putAll
        );
    }
}

