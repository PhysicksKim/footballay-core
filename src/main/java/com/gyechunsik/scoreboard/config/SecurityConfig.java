package com.gyechunsik.scoreboard.config;

import com.gyechunsik.scoreboard.security.CustomAnonymousAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String ANONYMOUS_KEY = "anonymousKey";

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(registry ->
                        registry.anyRequest().permitAll());
        http.anonymous(Customizer.withDefaults());
        http.formLogin(Customizer.withDefaults());
        http.sessionManagement(config -> {
            config.sessionCreationPolicy(SessionCreationPolicy.ALWAYS);
        });

        return http.build();
    }
}
