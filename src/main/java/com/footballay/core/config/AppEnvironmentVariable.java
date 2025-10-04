package com.footballay.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppEnvironmentVariable {
    @Value("${app.domain}")
    private String GYE_DOMAIN;
    @Value("${app.footballay.domain}")
    private String FOOTBALLAY_DOMAIN;
    @Value("${app.footballay.static.domain}")
    private String FOOTBALLAY_STATIC_DOMAIN;
    @Value("${app.gyechune.domain}")
    private String GYECHUNE_DOMAIN;

    public String getGYE_DOMAIN() {
        return this.GYE_DOMAIN;
    }

    public String getGYECHUNE_DOMAIN() {
        return this.GYECHUNE_DOMAIN;
    }

    public String getFOOTBALLAY_DOMAIN() {
        return this.FOOTBALLAY_DOMAIN;
    }

    public String getFOOTBALLAY_STATIC_DOMAIN() {
        return this.FOOTBALLAY_STATIC_DOMAIN;
    }

    public void setGYE_DOMAIN(final String GYE_DOMAIN) {
        this.GYE_DOMAIN = GYE_DOMAIN;
    }

    public void setGYECHUNE_DOMAIN(final String GYECHUNE_DOMAIN) {
        this.GYECHUNE_DOMAIN = GYECHUNE_DOMAIN;
    }

    public void setFOOTBALLAY_DOMAIN(final String FOOTBALLAY_DOMAIN) {
        this.FOOTBALLAY_DOMAIN = FOOTBALLAY_DOMAIN;
    }

    public void setFOOTBALLAY_STATIC_DOMAIN(final String FOOTBALLAY_STATIC_DOMAIN) {
        this.FOOTBALLAY_STATIC_DOMAIN = FOOTBALLAY_STATIC_DOMAIN;
    }
}
