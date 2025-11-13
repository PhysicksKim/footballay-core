package com.footballay.core.config.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private static final Logger log = getLogger(CustomAuthenticationFailureHandler.class);

    private final String baseFailureUrl;
    private final String allowedMainDomain;

    public CustomAuthenticationFailureHandler(
            @Value("${custom.login.failure-url:/login?error}") String baseFailureUrl,
            @Value("${custom.cookie.domain:footballay.com}") String allowedMainDomain
    ) {
        super();
        this.baseFailureUrl = baseFailureUrl;
        this.allowedMainDomain = allowedMainDomain;
        setDefaultFailureUrl(baseFailureUrl);
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception)
            throws IOException, ServletException {
        log.info("Authentication Fail Handler Called ex: {}", exception.getMessage());
        saveException(request, exception);

        String after = request.getParameter("after");
        String targetUrl = buildFailureTargetUrl(after);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private String buildFailureTargetUrl(String after) {
        String targetUrl = baseFailureUrl;

        if (after != null && !after.isBlank()) {
            if (isSafeRedirect(after, allowedMainDomain)) {
                String encoded = URLEncoder.encode(after, StandardCharsets.UTF_8);
                if (baseFailureUrl.contains("?")) {
                    targetUrl = baseFailureUrl + "&after=" + encoded;
                } else {
                    targetUrl = baseFailureUrl + "?after=" + encoded;
                }
            } else {
                log.warn("Blocked unsafe 'after' parameter on failure: {}", after);
            }
        }
        return targetUrl;
    }

    private boolean isSafeRedirect(String url, String allowedMainDomain) {
        if (url.startsWith("//")) {
            return false;
        }
        if (url.startsWith("/")) {
            return true;
        }
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            if (scheme == null) return false;
            if (!("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))) return false;
            String host = uri.getHost();
            if (host == null) return false;
            host = host.toLowerCase();
            String allowed = allowedMainDomain.toLowerCase();
            return host.equals(allowed) || host.endsWith("." + allowed);
        } catch (URISyntaxException e) {
            return false;
        }
    }
}
