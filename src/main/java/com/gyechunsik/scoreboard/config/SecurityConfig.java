package com.gyechunsik.scoreboard.config;

import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(registry ->
                        registry.anyRequest().permitAll());
        http.anonymous(Customizer.withDefaults());
        http.formLogin(AbstractHttpConfigurer::disable);
        http.headers(httpSecurityHeadersConfigurer -> httpSecurityHeadersConfigurer.frameOptions(
                custom -> custom.sameOrigin()
        ));
        return http.build();
    }
}
