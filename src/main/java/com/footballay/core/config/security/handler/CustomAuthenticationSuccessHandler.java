package com.footballay.core.config.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class CustomAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private static final Logger log = getLogger(CustomAuthenticationSuccessHandler.class);

    private final String allowedMainDomain;

    public CustomAuthenticationSuccessHandler(
            @Value("${custom.cookie.domain:footballay.com}") String allowedMainDomain
    ) {
        super();
        this.allowedMainDomain = allowedMainDomain;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws ServletException, IOException {

        log.info("Authentication Success handler called");

        String after = request.getParameter("after");
        if (StringUtils.hasText(after)) {
            if (isSafeRedirect(after, allowedMainDomain)) {
                log.info("Redirecting to safe 'after' URL: {}", after);
                clearAuthenticationAttributes(request);
                response.sendRedirect(after);
                return;
            } else {
                log.warn("Blocked unsafe redirect attempt to 'after' URL: {}", after);
            }
        }

        // 안전하지 않거나 after 파라미터가 없으면 기존 로직으로 처리
        super.onAuthenticationSuccess(request, response, authentication);
    }

    /**
     * 안전한 리다이렉트 여부 검사
     * - 상대경로("/x")는 허용
     * - protocol-relative ("//...") 는 금지
     * - 절대 URL은 host가 허용 도메인(예: footballay.com) 이거나 서브도메인인 경우만 허용
     */
    private boolean isSafeRedirect(String url, String allowedMainDomain) {
        if (url.startsWith("//")) {
            return false; // protocol-relative 는 허용하지 않음
        }
        if (url.startsWith("/")) {
            return true; // 같은 호스트 내부 이동으로 안전
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
