package com.gyechunsik.scoreboard.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;


@EnableJpaAuditing(
        auditorAwareRef = "principalAuditorAware",
        dateTimeProviderRef = "principalAuditorAware"
)
@Configuration
public class JpaConfig {
}
