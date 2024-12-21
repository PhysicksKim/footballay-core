package com.gyechunsik.scoreboard.web.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cloudfront.CloudFrontUtilities;
import software.amazon.awssdk.services.cloudfront.cookie.CookiesForCannedPolicy;
import software.amazon.awssdk.services.cloudfront.model.CannedSignerRequest;
import software.amazon.awssdk.services.cloudfront.url.SignedUrl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
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

    @Value("${aws.cloudfront.publicKeyPath}")
    private Resource publicKeyResource;

    private final String ADMIN_INDEX_PATH = "/chuncity/admin/index.html";
    private final String ADMIN_RESOURCE_PATH = "/chuncity/admin/*";

    private final CloudFrontUtilities cloudFrontUtilities = CloudFrontUtilities.create();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 쿠키 존재 여부 확인
     */
    public boolean hasSignedCookies(HttpServletRequest request) {
        Map<String, String> cookieMap = Arrays.stream(request.getCookies())
                .collect(Collectors.toMap(Cookie::getName, Cookie::getValue));

        return cookieMap.containsKey("CloudFront-Policy")
                && cookieMap.containsKey("CloudFront-Signature")
                && cookieMap.containsKey("CloudFront-Key-Pair-Id");
    }

    public String generateSignedUrlForFile(String filePath) {
        try {
            Instant expirationDate = Instant.now().plusSeconds(300); // 5분 뒤 만료
            String privateKeyPath = privateKeyResource.getFile().getPath();

            CannedSignerRequest cannedRequest = CannedSignerRequest.builder()
                    .resourceUrl(cloudfrontDomain + "/chuncity/admin/" + filePath)
                    .privateKey(Paths.get(privateKeyPath))
                    .keyPairId(keyPairId)
                    .expirationDate(expirationDate)
                    .build();

            SignedUrl signedUrl = cloudFrontUtilities.getSignedUrlWithCannedPolicy(cannedRequest);
            log.info("Generated Signed URL: {}", signedUrl.url());
            return signedUrl.url();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate signed URL for file: " + filePath, e);
        }
    }

    /**
     * 쿠키 발급 요청 코드 생성
     */
    public String generateCookieIssueToken(String redirectUri) {
        try {
            Map<String, Object> payload = Map.of(
                    "issuer", "physickskim",
                    "purpose", "issue-chuncity-admin-cloudfront-cookie",
                    "expiresAt", Instant.now().getEpochSecond() + 30,
                    "redirect", redirectUri
            );
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate cookie issue code", e);
        }
    }

    /**
     * 쿠키 발급 요청 코드 서명
     */
    public String signCookieIssueToken(String cookieIssueCode) {
        try {
            PrivateKey privateKey = loadPrivateKey();
            return signData(cookieIssueCode, privateKey);
        } catch (Exception e) {
            throw new RuntimeException("Failed to sign cookie issue code", e);
        }
    }

    /**
     * ADMIN index page 에 대한 Signed URL 생성
     */
    public String generateSignedUrlForIndexHtml() {
        try {
            CloudFrontUtilities cloudFrontUtilities = CloudFrontUtilities.create();
            Instant expirationDate = Instant.now().plusSeconds(300); // 5분 뒤 만료
            String privateKeyPath = privateKeyResource.getFile().getPath();

            CannedSignerRequest cannedRequest = CannedSignerRequest.builder()
                    .resourceUrl(cloudfrontDomain + ADMIN_INDEX_PATH)
                    .privateKey(Paths.get(privateKeyPath))
                    .keyPairId(keyPairId)
                    .expirationDate(expirationDate)
                    .build();

            SignedUrl signedUrl = cloudFrontUtilities.getSignedUrlWithCannedPolicy(cannedRequest);
            log.info("Generated Signed URL: {}", signedUrl.url());
            return signedUrl.url();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create signed URL", e);
        }
    }

    /**
     * Admin 리소스 전체에 접근할 수 있는 Signed Cookies 생성
     */
    public CookiesForCannedPolicy createSignedCookies() {
        try {
            Instant expirationDate = Instant.now().plusSeconds(1800); // 30분 뒤 만료
            String privateKeyPath = privateKeyResource.getFile().getPath();

            CannedSignerRequest cannedSignerRequest = CannedSignerRequest.builder()
                    .resourceUrl(cloudfrontDomain + ADMIN_RESOURCE_PATH)
                    .privateKey(Paths.get(privateKeyPath))
                    .keyPairId(keyPairId)
                    .expirationDate(expirationDate)
                    .build();

            CookiesForCannedPolicy signedCookies = cloudFrontUtilities.getCookiesForCannedPolicy(cannedSignerRequest);
            log.info("Generated Signed Cookies: Policy={}, Signature={}, KeyPairId={}",
                    signedCookies.expiresHeaderValue(),
                    signedCookies.signatureHeaderValue(),
                    signedCookies.keyPairIdHeaderValue());
            return signedCookies;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load private key file", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create signed cookies", e);
        }
    }

    private String loadKeyFile(Resource resource) throws IOException {
        return Files.readString(Paths.get(resource.getFile().getPath()), StandardCharsets.UTF_8)
                .replaceAll("-----.*-----", "") // 헤더 및 푸터 제거
                .replaceAll("\\s", ""); // 모든 공백 제거
    }

    private PrivateKey loadPrivateKey() throws Exception {
        String key = loadKeyFile(privateKeyResource);
        byte[] keyBytes = Base64.getDecoder().decode(key);
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
    }

    private String signData(String data, PrivateKey privateKey) throws Exception {
        Signature rsaSignature = Signature.getInstance("SHA256withRSA");
        rsaSignature.initSign(privateKey);
        rsaSignature.update(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(rsaSignature.sign());
    }

}
