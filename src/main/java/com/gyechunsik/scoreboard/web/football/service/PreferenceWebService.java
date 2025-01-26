package com.gyechunsik.scoreboard.web.football.service;

import com.gyechunsik.scoreboard.domain.football.preference.FootballPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PreferenceWebService {

    private final FootballPreferenceService footballPreferenceService;

    public boolean validateKey(String key) {
        return footballPreferenceService.validatePreferenceKey(key);
    }

}
