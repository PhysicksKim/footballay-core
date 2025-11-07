package com.footballay.core.config;

import com.footballay.core.config.security.handler.SpaCsrfTokenRequestHandler;
import org.springframework.beans.factory.annotation.Qualifier;
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
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SecurityConfig.class);
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
    private final CorsConfigurationSource corsConfigurationSource;

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
                    .failureUrl("/login?error")
                    .successHandler(authenticationSuccessHandler)
                    .failureHandler(authenticationFailureHandler))
            .anonymous(Customizer.withDefaults())
            .logout(configure ->
                configure.logoutSuccessHandler(logoutSuccessHandler))
            .headers(headers ->
                headers.frameOptions(custom -> custom.sameOrigin()))
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.ALWAYS))
            .exceptionHandling(exception ->
                exception.accessDeniedHandler(accessDeniedHandler))
            .rememberMe(configurer ->
                configurer.key(REMEMBER_ME_KEY)
                    .tokenValiditySeconds(rememberMeMaxAge)
                    .rememberMeCookieName(rememberMeName)
                    .useSecureCookie(true)
                    .alwaysRemember(false)
                    .rememberMeParameter("remember-me")
                    .userDetailsService(userDetailsService)
                    .tokenRepository(tokenRepository));
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
        repository.setCookieCustomizer(cookie -> {
            cookie.secure(true);
            cookie.httpOnly(false);
            cookie.sameSite("Strict");
        });
        return repository;
    }

    public SecurityConfig(final UserDetailsService userDetailsService, final PersistentTokenRepository tokenRepository, final AuthenticationFailureHandler authenticationFailureHandler, final AuthenticationSuccessHandler authenticationSuccessHandler, final LogoutSuccessHandler logoutSuccessHandler, final AccessDeniedHandler accessDeniedHandler, final CorsConfigurationSource corsConfigurationSource) {
        this.userDetailsService = userDetailsService;
        this.tokenRepository = tokenRepository;
        this.authenticationFailureHandler = authenticationFailureHandler;
        this.authenticationSuccessHandler = authenticationSuccessHandler;
        this.logoutSuccessHandler = logoutSuccessHandler;
        this.accessDeniedHandler = accessDeniedHandler;
        this.corsConfigurationSource = corsConfigurationSource;
    }
}
