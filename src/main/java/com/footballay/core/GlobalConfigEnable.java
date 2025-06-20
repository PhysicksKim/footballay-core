package com.footballay.core;

import com.footballay.core.infra.apisports.config.ApiSportsProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaAuditing
@EnableJpaRepositories
@EnableConfigurationProperties({ApiSportsProperties.class})
public class GlobalConfigEnable {
}
