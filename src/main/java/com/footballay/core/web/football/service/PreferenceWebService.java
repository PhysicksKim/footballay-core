package com.footballay.core.web.football.service;

import com.footballay.core.domain.football.preference.FootballPreferenceService;
import org.springframework.stereotype.Service;

@Service
public class PreferenceWebService {
    private final FootballPreferenceService footballPreferenceService;

    public boolean validateKey(String key) {
        return footballPreferenceService.validatePreferenceKey(key);
    }

    public PreferenceWebService(final FootballPreferenceService footballPreferenceService) {
        this.footballPreferenceService = footballPreferenceService;
    }
}
