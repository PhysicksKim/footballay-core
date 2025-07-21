package com.footballay.core.web.admin.football.service;

import com.footballay.core.config.AppEnvironmentVariable;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class AdminPageService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AdminPageService.class);
    private final AppEnvironmentVariable envVar;

    public String getAdminPageUri() {
        String domain = envVar.getFOOTBALLAY_STATIC_DOMAIN();
        return UriComponentsBuilder.newInstance().scheme("https").host(domain).pathSegment("footballay", "admin", "index.html").toUriString();
    }

    public AdminPageService(final AppEnvironmentVariable envVar) {
        this.envVar = envVar;
    }
}
