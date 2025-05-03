package com.footballay.core.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class AppEnvironmentVariable {

    @Value("${app.domain}")
    private String GYE_DOMAIN;

    @Value("${app.footballay.domain}")
    private String FOOTBALLAY_DOMAIN;

    @Value("${app.footballay.static.domain}")
    private String FOOTBALLAY_STATIC_DOMAIN;

}
