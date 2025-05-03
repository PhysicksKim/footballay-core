package com.footballay.core.web.admin.football.service;

import com.footballay.core.config.AppEnvironmentVariable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@RequiredArgsConstructor
@Service
public class AdminPageService {

    private final AppEnvironmentVariable envVar;

    public String getAdminPageUri() {
        String domain = envVar.getFOOTBALLAY_STATIC_DOMAIN();
        return UriComponentsBuilder.newInstance()
                .scheme("https")
                .host(domain)
                .pathSegment("footballay", "admin", "index.html")
                .toUriString();
    }

}
