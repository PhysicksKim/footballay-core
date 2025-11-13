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
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.slf4j.LoggerFactory.*;

@Component
public class MainDomainLoginEntryPoint implements AuthenticationEntryPoint {

    private static final Logger log = getLogger(MainDomainLoginEntryPoint.class);

    private final boolean mainDomainEnabled;
    private final String loginBaseUrl;
    private final String allowedMainDomain;
    private final AuthenticationEntryPoint delegate;

    public MainDomainLoginEntryPoint(
            @Value("${custom.login.main-domain-enabled:false}") boolean mainDomainEnabled,
            @Value("${custom.login.base-url:}") String loginBaseUrl,
            @Value("${custom.cookie.domain:footballay.com}") String allowedMainDomain
    ) {
        this.mainDomainEnabled = mainDomainEnabled;
        this.loginBaseUrl = loginBaseUrl;
        this.allowedMainDomain = allowedMainDomain;
        this.delegate = new LoginUrlAuthenticationEntryPoint("/login");

        if (this.mainDomainEnabled && (this.loginBaseUrl == null || this.loginBaseUrl.isBlank())) {
            throw new IllegalStateException(
                    "custom.login.main-domain-enabled=true 인데 custom.login.base-url 이 비어있습니다.");
        }
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException)
            throws IOException, ServletException {

        // 1) 로컬/DEV 등: 메인 도메인 강제 로그인 비활성화 시 기본 동작
        if (!mainDomainEnabled) {
            log.debug("MainDomainLoginEntryPoint disabled. Delegate to default /login entry point.");
            delegate.commence(request, response, authException);
            return;
        }

        // 2) nginx가 넘겨준 "원래 URL" 기준으로 after 생성
        String originalUrl = buildOriginalUrlFromHeadersOrRequest(request);

        // 2-1) open redirect 방지: allowedMainDomain(.footballay.com)만 허용
        if (!isSafeRedirect(originalUrl, allowedMainDomain)) {
            log.warn("Blocked unsafe originalUrl in entry point: {}", originalUrl);
            // 안전하지 않으면 그냥 메인 로그인으로만 보냄 (after 없이)
            response.sendRedirect(loginBaseUrl);
            return;
        }

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

    /**
     * nginx에서 설정한 X-Original-Uri / X-Original-Host / X-Forwarded-Proto 를 우선 사용하고,
     * 없으면 기존 request.getRequestURL() + queryString 으로 fallback 한다.
     */
    private String buildOriginalUrlFromHeadersOrRequest(HttpServletRequest request) {
        String originalUri  = request.getHeader("X-Original-Uri");
        String originalHost = request.getHeader("X-Original-Host");
        String scheme       = request.getHeader("X-Forwarded-Proto");

        if (!StringUtils.hasText(scheme)) {
            scheme = request.getScheme(); // fallback
        }

        if (StringUtils.hasText(originalUri) && StringUtils.hasText(originalHost)) {
            // nginx가 넘겨준 값 사용 → 외부에서 보이는 URL과 동일
            return scheme + "://" + originalHost + originalUri;
        }

        // fallback: 기존 방식 (admin.footballay.com/admin/ 같이 내부 경로가 섞일 수 있음)
        StringBuffer url = request.getRequestURL();
        String query = request.getQueryString();
        if (query != null) {
            url.append("?").append(query);
        }
        return url.toString();
    }

    private boolean isSafeRedirect(String url, String allowedMainDomain) {
        if (url == null || url.isBlank()) {
            return false;
        }
        if (url.startsWith("//")) {
            return false;
        }
        // 절대경로(도메인 없는 /path)는 허용 (같은 호스트로 가정)
        if (url.startsWith("/")) {
            return true;
        }
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            if (scheme == null) {
                return false;
            }
            if (!("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))) {
                return false;
            }
            String host = uri.getHost();
            if (host == null) {
                return false;
            }
            host = host.toLowerCase();
            String allowed = allowedMainDomain.toLowerCase();
            // footballay.com 또는 *.footballay.com 허용
            return host.equals(allowed) || host.endsWith("." + allowed);
        } catch (URISyntaxException e) {
            return false;
        }
    }
}