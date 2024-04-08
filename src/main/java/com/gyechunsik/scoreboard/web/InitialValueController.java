package com.gyechunsik.scoreboard.web;

import com.gyechunsik.scoreboard.domain.initval.Entity.DefaultTeam;
import com.gyechunsik.scoreboard.domain.initval.Entity.enums.DefaultTeamCodes;
import com.gyechunsik.scoreboard.domain.initval.Entity.enums.DefaultUniform;
import com.gyechunsik.scoreboard.domain.initval.Entity.enums.LeagueCategory;
import com.gyechunsik.scoreboard.domain.initval.Entity.enums.TeamSide;
import com.gyechunsik.scoreboard.domain.initval.InitialValueService;
import com.gyechunsik.scoreboard.web.request.RequestSaveDefaultTeam;
import com.gyechunsik.scoreboard.web.response.DefaultMatchResponse;
import com.gyechunsik.scoreboard.web.response.DefaultTeamResponse;
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
@RequestMapping("/admin/initval")
public class InitialValueController {

    private final InitialValueService initialValueService;

    @GetMapping
    public String getInitialValueForm(Model model) {
        model.addAttribute("leagueCategories", initialValueService.getLeagueCategories());
        model.addAttribute("uniforms", initialValueService.getUniforms());
        return "initval/form";
    }

    @PostMapping("/streamer")
    public String createStreamer(@RequestParam String streamerName) {
        initialValueService.createStreamer(streamerName);
        return "redirect:/admin/initval";
    }

    @PostMapping("/match")
    public String saveDefaultMatch(@RequestParam String streamerHash, @RequestParam String matchName) {
        initialValueService.saveDefaultMatch(streamerHash, matchName);
        return "redirect:/admin/initval";
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
        DefaultTeamResponse response = initialValueService.saveTeam(streamerHash, LeagueCategory.valueOf(category), DefaultTeamCodes.valueOf(teamCode), side, uniform);
        log.info("response :: {}", response);
        return response;
    }

    @GetMapping("/teamCodes")
    @ResponseBody
    public Map<String, String> getTeamCodes(@RequestParam String category) {
        log.info("getTeamCodes called :: category={}", category);
        LeagueCategory categoryEnum = LeagueCategory.valueOf(category);
        return initialValueService.getTeamCodes(categoryEnum);
    }

    @GetMapping("/league-categories")
    @ResponseBody
    public Map<String, String> getLeagueCategories() {
        return initialValueService.getLeagueCategories();
    }

    @GetMapping("/uniforms")
    @ResponseBody
    public List<String> getUniforms() {
        return initialValueService.getUniforms();
    }

    @GetMapping("/streamers")
    @ResponseBody
    public Map<String, String> getStreamers() {
        return initialValueService.getStreamers();
    }

    @GetMapping("/team")
    @ResponseBody
    public Map<String, Object> getCurrentTeam(@RequestParam String streamerHash) {
        DefaultTeamResponse[] team = initialValueService.findTeam(streamerHash);
        Map<String, Object> response = Map.of("teamA", team[0], "teamB", team[1]);
        log.info("response : {}", response);
        return response;
    }

    @GetMapping("/match")
    @ResponseBody
    public Map<String, Object> getCurrentMatch(@RequestParam String streamerHash) {
        DefaultMatchResponse response = initialValueService.findDefaultMatch(streamerHash);
        log.info("match response :: {}", response);
        return Map.of("match", response);
    }
}
