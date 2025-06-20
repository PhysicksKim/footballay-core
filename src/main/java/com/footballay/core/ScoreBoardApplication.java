package com.footballay.core;

import com.footballay.core.infra.apisports.config.ApiSportsProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
public class ScoreBoardApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScoreBoardApplication.class, args);
    }

}
