package com.footballay.core.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Configuration
public class WebConfig {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WebConfig.class);

    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                UrlBasedCorsConfigurationSource src = (UrlBasedCorsConfigurationSource) corsConfigurationSource;
                CorsConfiguration cfg = src.getCorsConfigurations().get("/**");
                log.info("CORS allow origins : {}", Arrays.toString(cfg.getAllowedOriginPatterns().toArray()));
                log.info("CORS allow methods : {}", Arrays.toString(cfg.getAllowedMethods().toArray()));
                log.info("CORS allow headers : {}", Arrays.toString(cfg.getAllowedHeaders().toArray()));
                log.info("CORS allow credentials : {}", cfg.getAllowCredentials());
                registry.addMapping("/**").allowedOriginPatterns(cfg.getAllowedOriginPatterns().toArray(new String[0])).allowedMethods(cfg.getAllowedMethods().toArray(new String[0])).allowedHeaders(cfg.getAllowedHeaders().toArray(new String[0])).allowCredentials(cfg.getAllowCredentials());
            }
        };
    }

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        return restTemplate;
    }

    public WebConfig(
            @Qualifier("corsConfigurationSource")
            final CorsConfigurationSource corsConfigurationSource
    ) {
        this.corsConfigurationSource = corsConfigurationSource;
    }
}
