package com.gyechunsik.scoreboard.web.admin;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cloudfront.CloudFrontUtilities;
import software.amazon.awssdk.services.cloudfront.cookie.CookiesForCustomPolicy;
import software.amazon.awssdk.services.cloudfront.model.CannedSignerRequest;
import software.amazon.awssdk.services.cloudfront.model.CustomSignerRequest;
import software.amazon.awssdk.services.cloudfront.url.SignedUrl;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Admin 페이지 관련 CloudFront Signed URL / Cookie 발급 서비스
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class AdminPageAwsService {

    @Value("${aws.cloudfront.domain}")
    private String cloudfrontDomain;

    @Value("${aws.cloudfront.keyPairId}")
    private String keyPairId;

    @Value("${aws.cloudfront.privateKeyPath}")
    private Resource privateKeyResource;

    private final String ADMIN_INDEX_PATH = "/index.html";
    private final String ADMIN_RESOURCE_PATH = "/chuncity/admin";
    private final String COOKIE_DOMAIN = ".gyechunsik.site";
    private final int COOKIE_MAX_AGE_SEC = 3600;
    private final int STATIC_FILE_EXPIRATION_SEC = 60;

    private final CloudFrontUtilities cloudFrontUtilities = CloudFrontUtilities.create();

    /**
     * 쿠키 존재 여부 확인
     *
     * <h4>Canned Policy 의 경우</h4>
     * CloudFront-Signature, CloudFront-Key-Pair-Id, CloudFront-Expires
     *
     * <h4>Custom Policy 의 경우</h4>
     * CloudFront-Signature, CloudFront-Key-Pair-Id, CloudFront-Policy
     *
     * @param request HTTP 요청
     * @return 쿠키 존재 여부
     */
    public boolean hasSignedCookies(HttpServletRequest request) {
        Map<String, String> cookieMap = collectCookies(request);
        return isContainCloudfrontCookies(cookieMap);
    }

    /**
     * ADMIN 페이지 접근 시 CloudFront Signed Cookies 발급
     * @param response HTTP 응답
     */
    public void setCloudFrontSignedCookie(HttpServletResponse response) {
        try {
            final Instant expirationDate = Instant.now().plusSeconds(COOKIE_MAX_AGE_SEC);

            CookiesForCustomPolicy cookiesForCustomPolicy = createSignedCookies(cloudfrontDomain+"/*", expirationDate);
            Map<String, String> cookiesMap = cookiesToMap(cookiesForCustomPolicy);
            setCookiesToHttpResponse(response, cookiesMap, COOKIE_DOMAIN, COOKIE_MAX_AGE_SEC);

            log.info("issued and set CloudFront signed Cookies for admin page");
        } catch (IOException e) {
            log.error("Failed to load private key file", e);
            throw new RuntimeException(e);
        } catch (Exception e) {
            log.error("Failed to create custom signed cookies", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * ADMIN index page 에 대한 Signed URL 생성
     * @return Admin Index html 의 Signed URL
     */
    public String generateSignedUrlForAdminPage() {
        try {
            Instant expirationDate = Instant.now().plusSeconds(STATIC_FILE_EXPIRATION_SEC);
            CannedSignerRequest cannedRequest = createCannedRequest(ADMIN_INDEX_PATH, expirationDate);
            SignedUrl signedUrl = cloudFrontUtilities.getSignedUrlWithCannedPolicy(cannedRequest);
            return signedUrl.url();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create signed URL", e);
        }
    }

    /**
     * ADMIN html 의 정적 파일에 대한 Signed URL 생성
     * @param url 서명하고자 하는 정적 파일의 URL
     * @return Signed URL
     */
    public String generateSignedUrlForUrl(String url) {
        try {
            if(!url.startsWith("/")) {
                url = "/" + url;
            }

            Instant expirationDate = Instant.now().plusSeconds(STATIC_FILE_EXPIRATION_SEC);
            CannedSignerRequest cannedRequest = createCannedRequest(url, expirationDate);
            SignedUrl signedUrl = cloudFrontUtilities.getSignedUrlWithCannedPolicy(cannedRequest);
            log.info("Generated Signed URL: {}", signedUrl.url());
            return signedUrl.url();
        } catch (Exception e) {
            log.error("Failed to generate signed URL for file: {}", url, e);
            throw new RuntimeException("Failed to generate signed URL for file: " + url, e);
        }
    }

    private CannedSignerRequest createCannedRequest(String url, Instant expirationDate) throws Exception {
        Path privateKeyPath = Paths.get(privateKeyResource.getFile().getPath());
        String resourceUrl = cloudfrontDomain + ADMIN_RESOURCE_PATH + url;
        log.info("create canned signer request for resource=[{}],expirationDate=[{}]", resourceUrl, expirationDate);
        return CannedSignerRequest.builder()
                .resourceUrl(resourceUrl)
                .keyPairId(keyPairId)
                .privateKey(privateKeyPath)
                .expirationDate(expirationDate)
                .build();
    }

    private CookiesForCustomPolicy createSignedCookies(String resourceUrl, Instant expirationDate) throws Exception {
        Path privateKeyPath = Paths.get(privateKeyResource.getFile().getPath());
        CustomSignerRequest customPolicyRequest = CustomSignerRequest.builder()
                .keyPairId(keyPairId)
                .privateKey(privateKeyPath)
                .resourceUrl(resourceUrl)
                .expirationDate(expirationDate)
                .build();
        return cloudFrontUtilities.getCookiesForCustomPolicy(customPolicyRequest);
    }

    /**
     * aws util 에서 제공하는 cookieValue 는 "{cookieName}={cookieValue}" 로 구성되어 있습니다.
     * 이때 name 과 value 둘 다 = 문자를 포함하지 않습니다.
     * aws util 에서는 base64 인코딩 후 "=" 문자가 포함되어 있다면 "_" 로 치환되어 있습니다.
     * @see <a href="https://docs.aws.amazon.com/ko_kr/AmazonCloudFront/latest/DeveloperGuide/private-content-setting-signed-cookie-canned-policy.html">
     *     Amazon CloudFront 개발자 가이드 - 미리 준비된 정책을 사용하여 서명된 쿠키 설정</a>
     * @param cookiesForCustomPolicy aws util 에서 제공하는 Custom Policy 쿠키
     * @return cookieName, cookieValue 로 구성된 Map
     */
    private Map<String, String> cookiesToMap(CookiesForCustomPolicy cookiesForCustomPolicy) {
        String[] policies = cookiesForCustomPolicy.policyHeaderValue().split("=");
        String[] keyPairIds = cookiesForCustomPolicy.keyPairIdHeaderValue().split("=");
        String[] signatures = cookiesForCustomPolicy.signatureHeaderValue().split("=");
        return Map.of(
                policies[0], policies[1],
                keyPairIds[0], keyPairIds[1],
                signatures[0], signatures[1]
        );
    }

    private static boolean isContainCloudfrontCookies(Map<String, String> cookieMap) {
        return (cookieMap.containsKey("CloudFront-Policy") || cookieMap.containsKey("CloudFront-Expires"))
                && cookieMap.containsKey("CloudFront-Signature")
                && cookieMap.containsKey("CloudFront-Key-Pair-Id");
    }

    private static void setCookiesToHttpResponse(
            HttpServletResponse response,
            Map<String, String> cookiesMap,
            String cookieDomain,
            long maxAgeInSeconds
    ) {
        for(Map.Entry<String, String> cookieEntry : cookiesMap.entrySet()) {
            ResponseCookie cookie = createCookieFrom(cookieEntry, cookieDomain, maxAgeInSeconds);
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        }
    }

    private static @NotNull ResponseCookie createCookieFrom(Map.Entry<String, String> cookieEntry, String cookieDomain, long maxAgeInSeconds) {
        return ResponseCookie.from(cookieEntry.getKey(), cookieEntry.getValue())
                .domain(cookieDomain)
                .path("/")
                .secure(true)
                .httpOnly(true)
                .sameSite("None")
                .maxAge(maxAgeInSeconds)
                .build();
    }

    private static @NotNull Map<String, String> collectCookies(HttpServletRequest request) {
        return Arrays.stream(request.getCookies())
                .collect(Collectors.toMap(Cookie::getName, Cookie::getValue));
    }

}
