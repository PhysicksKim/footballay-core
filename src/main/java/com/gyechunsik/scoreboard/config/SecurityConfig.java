package com.gyechunsik.scoreboard.config;

import com.gyechunsik.scoreboard.config.security.handler.SpaCsrfTokenRequestHandler;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Value("${custom.login.remember-me-key}")
    private String REMEMBER_ME_KEY;

    @Value("${cookies.remember-me.max-age}")
    private int rememberMeMaxAge;
    @Value("${cookies.remember-me.name}")
    private String rememberMeName;

    private final UserDetailsService userDetailsService;
    private final PersistentTokenRepository tokenRepository;

    private final AuthenticationFailureHandler authenticationFailureHandler;
    private final AuthenticationSuccessHandler authenticationSuccessHandler;
    private final LogoutSuccessHandler logoutSuccessHandler;
    private final AccessDeniedHandler accessDeniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfConfigurationSource())
                        .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler()) // 커스텀 핸들러 설정
                        .ignoringRequestMatchers(ignoreExceptLoginPost())
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/**").hasAnyRole("ADMIN")
                        .anyRequest().permitAll())
                .formLogin(form -> form.permitAll()
                        .failureUrl("/login?error")
                        .successHandler(authenticationSuccessHandler)
                        .failureHandler(authenticationFailureHandler))
                .anonymous(Customizer.withDefaults())
                .logout(configure ->
                        configure.logoutSuccessHandler(logoutSuccessHandler))
                .headers(headers -> headers.frameOptions(
                        custom -> custom.sameOrigin()
                ))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
                )
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .rememberMe(configurer ->
                    configurer
                            .key(REMEMBER_ME_KEY)
                            .tokenValiditySeconds(rememberMeMaxAge)
                            .rememberMeCookieName(rememberMeName)
                            .useSecureCookie(true)
                            .alwaysRemember(false)
                            .rememberMeParameter("remember-me")
                            .userDetailsService(userDetailsService)
                            .tokenRepository(tokenRepository)
                );

        return http.build();
    }

    private RequestMatcher ignoreExceptLoginPost() {
        return request -> {
            String uri = request.getRequestURI();
            String method = request.getMethod();

            // /login 이면서 POST인 경우 => CSRF 필터 적용
            if ("/login".equals(uri) && "POST".equalsIgnoreCase(method)) {
                log.info("CSRF 필터 적용: {}", uri);
                return false; // CSRF 필터 적용
            }

            // 그 외 모든 요청 => CSRF 무시
            log.info("CSRF 필터 무시: {}", uri);
            return true;
        };
    }

    private CsrfTokenRepository csrfConfigurationSource() {
        CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        // 쿠키 및 헤더 이름은 기본값을 사용합니다.
        // repository.setCookieName("XSRF-TOKEN");
        // repository.setHeaderName("X-XSRF-TOKEN");
        repository.setCookiePath("/");
        repository.setCookieCustomizer(cookie -> {
            cookie.secure(true);
            cookie.httpOnly(false);
            cookie.sameSite("Strict");
        });
        return repository;
    }


}
