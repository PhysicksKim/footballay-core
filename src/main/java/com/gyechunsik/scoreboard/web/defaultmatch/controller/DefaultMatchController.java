package com.gyechunsik.scoreboard.web.defaultmatch.controller;

import com.gyechunsik.scoreboard.domain.defaultmatch.entity.enums.DefaultTeamCodes;
import com.gyechunsik.scoreboard.domain.defaultmatch.entity.enums.DefaultUniform;
import com.gyechunsik.scoreboard.domain.defaultmatch.entity.enums.LeagueCategory;
import com.gyechunsik.scoreboard.domain.defaultmatch.entity.enums.TeamSide;
import com.gyechunsik.scoreboard.domain.defaultmatch.DefaultMatchService;
import com.gyechunsik.scoreboard.web.defaultmatch.request.RequestSaveDefaultTeam;
import com.gyechunsik.scoreboard.web.defaultmatch.response.DefaultMatchResponse;
import com.gyechunsik.scoreboard.web.defaultmatch.response.DefaultTeamResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Controller
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/admin/defaultmatch")
public class DefaultMatchController {

    private final DefaultMatchService defaultMatchService;

    @GetMapping
    public String getDefaultMatchForm(Model model) {
        model.addAttribute("leagueCategories", defaultMatchService.getLeagueCategories());
        model.addAttribute("uniforms", defaultMatchService.getUniforms());
        return "defaultmatch/form";
    }

    @PostMapping("/streamer")
    public String createStreamer(@RequestParam String streamerName) {
        defaultMatchService.createStreamer(streamerName);
        return "redirect:/admin/defaultmatch";
    }

    @PostMapping("/match")
    public String saveDefaultMatch(@RequestParam String streamerHash, @RequestParam String matchName) {
        defaultMatchService.saveDefaultMatch(streamerHash, matchName);
        return "redirect:/admin/defaultmatch";
    }

    @PostMapping("/team")
    @ResponseBody
    public DefaultTeamResponse saveTeam(
            @RequestBody RequestSaveDefaultTeam request
    ) {
        String streamerHash = request.getStreamerHash();
        String category = request.getCategory();
        String teamCode = request.getTeamCode();
        TeamSide side = request.getSide();
        DefaultUniform uniform = request.getUniform();

        log.info("saveTeam called :: streamerHash={}, category={}, teamCode={}, side={}, uniform={}", streamerHash, category, teamCode, side, uniform);
        DefaultTeamResponse response = defaultMatchService.saveTeam(streamerHash, LeagueCategory.valueOf(category), DefaultTeamCodes.valueOf(teamCode), side, uniform);
        log.info("response :: {}", response);
        return response;
    }

    @GetMapping("/teamCodes")
    @ResponseBody
    public Map<String, String> getTeamCodes(@RequestParam String category) {
        log.info("getTeamCodes called :: category={}", category);
        LeagueCategory categoryEnum = LeagueCategory.valueOf(category);
        return defaultMatchService.getTeamCodes(categoryEnum);
    }

    @GetMapping("/league-categories")
    @ResponseBody
    public Map<String, String> getLeagueCategories() {
        return defaultMatchService.getLeagueCategories();
    }

    @GetMapping("/uniforms")
    @ResponseBody
    public List<String> getUniforms() {
        return defaultMatchService.getUniforms();
    }

    @GetMapping("/streamers")
    @ResponseBody
    public Map<String, String> getStreamers() {
        return defaultMatchService.getStreamers();
    }

    @GetMapping("/team")
    @ResponseBody
    public Map<String, Object> getCurrentTeam(@RequestParam String streamerHash) {
        DefaultTeamResponse[] team = defaultMatchService.findTeam(streamerHash);
        Map<String, Object> response = Map.of("teamA", team[0], "teamB", team[1]);
        log.info("response : {}", response);
        return response;
    }

    @GetMapping("/match")
    @ResponseBody
    public Map<String, Object> getCurrentMatch(@RequestParam String streamerHash) {
        DefaultMatchResponse response = defaultMatchService.findDefaultMatch(streamerHash);
        log.info("match response :: {}", response);
        return Map.of("match", response);
    }
}
