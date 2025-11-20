package com.footballay.core.config;

import com.footballay.core.config.security.handler.SpaCsrfTokenRequestHandler;
import com.footballay.core.config.security.login.MainDomainLoginEntryPoint;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
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
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.slf4j.LoggerFactory.*;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    private static final Logger log = getLogger(SecurityConfig.class);


    private final boolean isLocal;

    private final String rememberMeKey;
    private final int rememberMeMaxAge;
    private final String rememberMeName;
    private final String csrfCookieSameSite;
    private final String cookieDomain;

    private final UserDetailsService userDetailsService;
    private final PersistentTokenRepository tokenRepository;
    private final AuthenticationFailureHandler authenticationFailureHandler;
    private final AuthenticationSuccessHandler authenticationSuccessHandler;
    private final LogoutSuccessHandler logoutSuccessHandler;
    private final AccessDeniedHandler accessDeniedHandler;
    private final CorsConfigurationSource corsConfigurationSource;
    private final MainDomainLoginEntryPoint mainDomainLoginEntryPoint;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // 커스텀 핸들러 설정
        // .failureHandler(authenticationFailureHandler)
        http.cors(cors ->
            cors.configurationSource(corsConfigurationSource))
            .csrf(csrf ->
                csrf.csrfTokenRepository(csrfConfigurationSource())
                    .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
                    .ignoringRequestMatchers(ignoreExceptLoginPost()))
            .authorizeHttpRequests(auth ->
                auth.requestMatchers("/admin/**")
                    .hasAnyRole("ADMIN")
                    .anyRequest().permitAll())
            .formLogin(form ->
                form.permitAll()
                    .successHandler(authenticationSuccessHandler)
                    .failureHandler(authenticationFailureHandler))
            .anonymous(Customizer.withDefaults())
            .logout(configure ->
                configure.logoutSuccessHandler(logoutSuccessHandler))
            .headers(headers ->
                headers.frameOptions(custom -> custom.sameOrigin()))
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .exceptionHandling(exception ->
                exception
                    .authenticationEntryPoint(mainDomainLoginEntryPoint)
                    .accessDeniedHandler(accessDeniedHandler))
            .rememberMe(configurer ->{
                    configurer
                            .key(rememberMeKey)
                            .tokenValiditySeconds(rememberMeMaxAge)
                            .rememberMeCookieName(rememberMeName)
                            .useSecureCookie(!isLocal)          // 로컬에서는 secure=false
                            .alwaysRemember(false)
                            .rememberMeParameter("remember-me")
                            .userDetailsService(userDetailsService)
                            .tokenRepository(tokenRepository);
                    // 로컬이 아닐 때만 도메인 설정
                    if (!isLocal && cookieDomain != null && !cookieDomain.isBlank()) {
                        configurer.rememberMeCookieDomain(cookieDomain);
                    }
                    });
        return http.build();
    }

    private RequestMatcher ignoreExceptLoginPost() {
        return request -> {
            String uri = request.getRequestURI();
            String method = request.getMethod();
            // /login 이면서 POST인 경우 => CSRF 필터 적용
            if ("/login".equals(uri) && "POST".equalsIgnoreCase(method)) {
                log.info("login POST request. CSRF 필터 적용: {}", uri);
                return false; // CSRF 필터 적용
            }
            return true;
        };
    }

    private CsrfTokenRepository csrfConfigurationSource() {
        CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repository.setCookiePath("/");
        repository.setCookieName("XSRF-TOKEN");
        repository.setHeaderName("X-XSRF-TOKEN");
        repository.setParameterName("_csrf");

        repository.setCookieCustomizer(cookie -> {
            cookie.httpOnly(false);
            cookie.sameSite(csrfCookieSameSite);
            cookie.secure(!isLocal);
            // 로컬에서는 Domain 을 아예 설정하지 않음 (host-only cookie)
            if (!isLocal && cookieDomain != null && !cookieDomain.isBlank()) {
                cookie.domain(cookieDomain);
            }
        });

        return repository;
    }

    public SecurityConfig(
                Environment env,
                @Value("${csrf.cookie.samesite:lax}") String csrfCookieSameSite,
                @Value("${custom.cookie.domain:footballay.com}") String cookieDomain,
                @Value("${custom.login.remember-me-key}") String rememberMeKey,
                @Value("${cookies.remember-me.max-age}") int rememberMeMaxAge,
                @Value("${cookies.remember-me.name}") String rememberMeName,
                final UserDetailsService userDetailsService,
                final PersistentTokenRepository tokenRepository,
                final AuthenticationFailureHandler authenticationFailureHandler,
                final AuthenticationSuccessHandler authenticationSuccessHandler,
                final LogoutSuccessHandler logoutSuccessHandler,
                final AccessDeniedHandler accessDeniedHandler,
                final CorsConfigurationSource corsConfigurationSource,
                final MainDomainLoginEntryPoint mainDomainLoginEntryPoint) {
        this.csrfCookieSameSite = csrfCookieSameSite;
        this.cookieDomain = cookieDomain;
        this.rememberMeKey = rememberMeKey;
        this.rememberMeMaxAge = rememberMeMaxAge;
        this.rememberMeName = rememberMeName;
        this.userDetailsService = userDetailsService;
        this.tokenRepository = tokenRepository;
        this.authenticationFailureHandler = authenticationFailureHandler;
        this.authenticationSuccessHandler = authenticationSuccessHandler;
        this.logoutSuccessHandler = logoutSuccessHandler;
        this.accessDeniedHandler = accessDeniedHandler;
        this.corsConfigurationSource = corsConfigurationSource;
        this.mainDomainLoginEntryPoint = mainDomainLoginEntryPoint;

        this.isLocal = env.acceptsProfiles(Profiles.of("local"));
    }
}
