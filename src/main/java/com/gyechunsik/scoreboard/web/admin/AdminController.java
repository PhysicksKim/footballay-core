package com.gyechunsik.scoreboard.web.admin;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.services.cloudfront.cookie.CookiesForCannedPolicy;

import java.io.UnsupportedEncodingException;

@Slf4j
@RequiredArgsConstructor
@Controller
public class AdminController {

    private final AdminPageAwsService adminPageAwsService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${spring.profiles.active:}")
    private String activeProfile;

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> adminIndexPage(Authentication authentication, HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
        log.info("authenticated user:{}",authentication);
        // boolean isDev = activeProfile.contains("dev");
        boolean isDev = false;
        // boolean hasCloudfrontSignedCookie = adminPageAwsService.hasSignedCookies(request);
        // if (!isDev && !hasCloudfrontSignedCookie) {
        //     // prod 환경: 자체 쿠키 발급
        //     // CookiesForCannedPolicy signedCookies = adminPageAwsService.createSignedCookies();
        //     // addSignedCookiesToResponse(response, signedCookies);
        //     adminPageAwsService.createCustomSignedCookies(response);
        //     log.info("prod admin page signed cookies added to response");
        // }

        adminPageAwsService.setCustomSignedCookiesWithUtil(response);

        // Signed URL 을 이용하여 index.html 가져오기
        String signedUrl = adminPageAwsService.generateSignedUrlForIndexHtml();
        String html = restTemplate.getForObject(signedUrl, String.class);
        if(isDev) {
            html = rewriteStaticFilePathsForLocalhost(html);
        }
        return ResponseEntity.ok().body(html);
    }

    // Admin 정적 파일 프록싱
    @GetMapping("/admin/**")
    @CrossOrigin(origins = "https://localhost:8083", allowCredentials = "true")
    public ResponseEntity<byte[]> serveStaticFile(HttpServletRequest request) {
        boolean isDev = activeProfile.contains("dev");
        String requestedPath = request.getRequestURI().substring("/admin/".length());

        if (isDev) {
            // CloudFront에서 Signed URL 생성
            String signedUrl = adminPageAwsService.generateSignedUrlForFile(requestedPath);
            log.info("Signed URL for {}: {}", requestedPath, signedUrl);

            // Signed URL로 파일 가져오기
            try {
                ResponseEntity<byte[]> response = restTemplate.getForEntity(signedUrl, byte[].class);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(response.getHeaders().getContentType());
                headers.setContentLength(response.getBody().length);
                return ResponseEntity.ok().headers(headers).body(response.getBody());
            } catch (Exception e) {
                log.error("Failed to fetch file from Signed URL: {}", signedUrl, e);
                return ResponseEntity.status(404).body(null);
            }
        }

        // 프로덕션 환경에서는 직접 제공하지 않음
        return ResponseEntity.status(403).body(null);
    }

    /**
     * Canned Policy 기반 서명된 쿠키 발급 엔드포인트
     */
    @GetMapping("/admin/issue-canned-cookies")
    public ResponseEntity<String> issueCannedSignedCookies(HttpServletResponse response) {
        // 1. 서명된 쿠키 생성
        CookiesForCannedPolicy signedCookies = adminPageAwsService.createSignedCookiesForCannedPolicy();

        // 2. 쿠키 속성 설정
        String cookieDomain = ".gyechunsik.site"; // 필요에 따라 변경
        long maxAgeInSeconds = 3600; // 1시간

        // 3. CloudFront-Expires 쿠키 설정
        String[] expireNameValues = signedCookies.expiresHeaderValue().split("=");
        ResponseCookie expiresCookie = ResponseCookie.from(expireNameValues[0],expireNameValues[1])
                .domain(cookieDomain)
                .path("/")
                .secure(true)
                .httpOnly(true)
                .sameSite("None")
                .maxAge(maxAgeInSeconds)
                .build();

        // 4. CloudFront-Signature 쿠키 설정
        String[] signatureNameValues = signedCookies.signatureHeaderValue().split("=");
        ResponseCookie signatureCookie = ResponseCookie.from(signatureNameValues[0],signatureNameValues[1])
                .domain(cookieDomain)
                .path("/")
                .secure(true)
                .httpOnly(true)
                .sameSite("None")
                .maxAge(maxAgeInSeconds)
                .build();

        // 5. CloudFront-Key-Pair-Id 쿠키 설정
        String[] keyPairIdNameValues = signedCookies.keyPairIdHeaderValue().split("=");
        ResponseCookie keyPairIdCookie = ResponseCookie.from(keyPairIdNameValues[0],keyPairIdNameValues[1])
                .domain(cookieDomain)
                .path("/")
                .secure(true)
                .httpOnly(true)
                .sameSite("None")
                .maxAge(maxAgeInSeconds)
                .build();

        // 6. 응답에 쿠키 추가
        response.addHeader(HttpHeaders.SET_COOKIE, expiresCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, signatureCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, keyPairIdCookie.toString());

        return ResponseEntity.ok("Canned Signed Cookies have been issued.");
    }

    private void addSignedCookiesToResponse(HttpServletResponse response, CookiesForCannedPolicy signedCookies) {
        addCookie(response, signedCookies.expiresHeaderValue());
        addCookie(response, signedCookies.signatureHeaderValue());
        addCookie(response, signedCookies.keyPairIdHeaderValue());
    }

    private void addCookie(HttpServletResponse response, String cookieString) {
        String[] parts = cookieString.split("=", 2); // "key=value" 형태로 분리
        if (parts.length != 2) {
            log.warn("Invalid cookie string: {}", cookieString);
            return;
        }

        ResponseCookie cookie = ResponseCookie.from(parts[0], parts[1])
                .domain(".gyechunsik.site")
                .path("/")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private String rewriteStaticFilePathsForLocalhost(String html) {
        // 정규 표현식을 사용하여 HTML 내부의 경로 변경
        String rewrittenHtml = html
                .replaceAll("https://static.gyechunsik.site/chuncity/admin/", "https://localhost:8083/admin/");
        log.info("Rewritten HTML for localhost: {}", rewrittenHtml);
        return rewrittenHtml;
    }

}
