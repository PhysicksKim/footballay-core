package com.footballay.core;

import com.footballay.core.infra.apisports.shared.config.ApiSportsProperties;
import com.footballay.core.infra.dataquality.config.DataQualityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaAuditing
@EnableJpaRepositories
@EnableConfigurationProperties({ApiSportsProperties.class, DataQualityProperties.class})
public class GlobalConfigEnable {
}
