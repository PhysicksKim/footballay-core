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
    private String domain;

}
