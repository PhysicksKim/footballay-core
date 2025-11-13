package com.footballay.core.config.security.login;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.slf4j.LoggerFactory.*;

@Component
public class MainDomainLoginEntryPoint implements AuthenticationEntryPoint {

    private static final Logger log = getLogger(MainDomainLoginEntryPoint.class);

    private final boolean mainDomainEnabled;
    private final String loginBaseUrl;
    private final AuthenticationEntryPoint delegate;

    public MainDomainLoginEntryPoint(
            @Value("${custom.login.main-domain-enabled:false}") boolean mainDomainEnabled,
            @Value("${custom.login.base-url:}") String loginBaseUrl
    ) {
        this.mainDomainEnabled = mainDomainEnabled;
        this.loginBaseUrl = loginBaseUrl;
        this.delegate = new LoginUrlAuthenticationEntryPoint("/login");

        if (this.mainDomainEnabled && (this.loginBaseUrl == null || this.loginBaseUrl.isBlank())) {
            throw new IllegalStateException(
                    "custom.login.main-domain-enabled=true 인데 custom.login.base-url 이 비어있습니다.");
        }
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        // 1) 로컬/DEV 등: 메인 도메인 로그인 강제가 비활성화인 경우
        if (!mainDomainEnabled) {
            log.debug("MainDomainLoginEntryPoint disabled. Delegate to default /login entry point.");
            delegate.commence(request, response, authException);
            return;
        }

        // 2) PROD 등: footballay.com/login 으로 보내는 커스텀 로직
        StringBuffer url = request.getRequestURL();
        String query = request.getQueryString();
        if (query != null) {
            url.append("?").append(query);
        }
        String originalUrl = url.toString();

        String encoded = URLEncoder.encode(originalUrl, StandardCharsets.UTF_8);

        String redirectUrl;
        if (loginBaseUrl.contains("?")) {
            redirectUrl = loginBaseUrl + "&after=" + encoded;
        } else {
            redirectUrl = loginBaseUrl + "?after=" + encoded;
        }

        log.info("Redirecting unauthenticated request to main login URL: {}", redirectUrl);
        response.sendRedirect(redirectUrl);
    }

}
