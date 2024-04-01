package com.gyechunsik.scoreboard.web;

import com.gyechunsik.scoreboard.domain.initval.Entity.enums.DefaultTeamCodes;
import com.gyechunsik.scoreboard.domain.initval.Entity.enums.DefaultUniform;
import com.gyechunsik.scoreboard.domain.initval.Entity.enums.LeagueCategory;
import com.gyechunsik.scoreboard.domain.initval.Entity.enums.TeamSide;
import com.gyechunsik.scoreboard.domain.initval.InitialValueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Controller
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
    public String saveTeam(@RequestParam String streamerHash,
                           @RequestParam String category,
                           @RequestParam String teamCode,
                           @RequestParam TeamSide side,
                           @RequestParam DefaultUniform uniform) {
        log.info("saveTeam called :: streamerHash={}, category={}, teamCode={}, side={}, uniform={}", streamerHash, category, teamCode, side, uniform);
        initialValueService.saveTeam(streamerHash, LeagueCategory.valueOf(category), DefaultTeamCodes.valueOf(teamCode), side, uniform);
        return "redirect:/admin/initval";
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
}
