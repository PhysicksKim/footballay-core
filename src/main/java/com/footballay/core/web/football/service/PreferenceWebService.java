package com.footballay.core.web.football.service;

import com.footballay.core.domain.football.preference.FootballPreferenceService;
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
