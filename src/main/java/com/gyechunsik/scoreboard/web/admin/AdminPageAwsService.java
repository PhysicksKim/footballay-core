package com.gyechunsik.scoreboard.web.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cloudfront.CloudFrontUtilities;
import software.amazon.awssdk.services.cloudfront.cookie.CookiesForCannedPolicy;
import software.amazon.awssdk.services.cloudfront.cookie.CookiesForCustomPolicy;
import software.amazon.awssdk.services.cloudfront.cookie.SignedCookie;
import software.amazon.awssdk.services.cloudfront.model.CannedSignerRequest;
import software.amazon.awssdk.services.cloudfront.model.CustomSignerRequest;
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
import java.util.List;
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

    public void setCustomSignedCookiesWithUtil(HttpServletResponse response) {
        try {
            String resourceUrl = "https://static.gyechunsik.site/*";
            Instant now = Instant.now();
            Instant expirationDate = now.plusSeconds(3600);
            CookiesForCustomPolicy cookiesForCustomPolicy = cloudFrontUtilities.getCookiesForCustomPolicy(CustomSignerRequest.builder()
                    .resourceUrl(resourceUrl)
                    .privateKey(Paths.get(privateKeyResource.getFile().getPath()))
                    .keyPairId(keyPairId)
                    .expirationDate(expirationDate)
                    .build());
            String[] policy = cookiesForCustomPolicy.policyHeaderValue().split("=");
            String[] keyPairId = cookiesForCustomPolicy.keyPairIdHeaderValue().split("=");
            String[] signature = cookiesForCustomPolicy.signatureHeaderValue().split("=");

            /*
            const cookieDomain = ".gyechunsik.site";
            const maxAgeInSeconds = 3600; // 1시간
            const expires = new Date(Date.now() + maxAgeInSeconds * 1000).toUTCString();
             */
            String cookieDomain = ".gyechunsik.site";
            long maxAgeInSeconds = expirationDate.getEpochSecond() - now.getEpochSecond();

            /*
            const cookies = Object.entries(signedCookies).map(
              ([name, value]) =>
              `${name}=${value}; Domain=${cookieDomain}; Path=/; Secure; HttpOnly; SameSite=None; Expires=${expires}`
            );
            */
            ResponseCookie policyCookie = ResponseCookie.from(policy[0], policy[1])
                    .domain(cookieDomain)
                    .path("/")
                    .secure(true)
                    .httpOnly(true)
                    .sameSite("None")
                    .maxAge(maxAgeInSeconds)
                    .build();

            ResponseCookie keyPairIdCookie = ResponseCookie.from(keyPairId[0], keyPairId[1])
                    .domain(cookieDomain)
                    .path("/")
                    .secure(true)
                    .httpOnly(true)
                    .sameSite("None")
                    .maxAge(maxAgeInSeconds)
                    .build();

            ResponseCookie signatureCookie = ResponseCookie.from(signature[0], signature[1])
                    .domain(cookieDomain)
                    .path("/")
                    .secure(true)
                    .httpOnly(true)
                    .sameSite("None")
                    .maxAge(maxAgeInSeconds)
                    .build();

            log.info("Policy: {}", policyCookie.toString());
            log.info("KeyPairId: {}", keyPairIdCookie.toString());
            log.info("Signature: {}", signatureCookie.toString());

            response.addHeader(HttpHeaders.SET_COOKIE, policyCookie.toString());
            response.addHeader(HttpHeaders.SET_COOKIE, keyPairIdCookie.toString());
            response.addHeader(HttpHeaders.SET_COOKIE, signatureCookie.toString());
        } catch (IOException e) {
            log.error("Failed to load private key file", e);
            throw new RuntimeException(e);
        } catch (Exception e) {
            log.error("Failed to create custom signed cookies", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Custom Policy 기반의 서명된 쿠키 생성
     */
    public void createCustomSignedCookies(HttpServletResponse response) {
        try {
            // 1. Custom Policy JSON 생성
            long epochTime = Instant.now().getEpochSecond() + 3600; // 1시간 유효
            String resourceUrl = cloudfrontDomain + "/*"; // 정확한 리소스 경로 포함
            String policyJson = generatePolicyJson(resourceUrl, epochTime);
            log.info("Custom Policy JSON: {}", policyJson);

            CookiesForCustomPolicy cookiesForCustomPolicy = cloudFrontUtilities.getCookiesForCustomPolicy(CustomSignerRequest.builder()
                    .resourceUrl(resourceUrl)
                    .privateKey(Paths.get(privateKeyResource.getFile().getPath()))
                    .keyPairId(keyPairId)
                    .expirationDate(Instant.now().plusSeconds(3600))
                    .build());

            // 2. 정책 서명(Sign) 생성
            PrivateKey privateKey = loadPrivateKey();
            String signature = signPolicy(policyJson, privateKey);
            log.info("Signature (Base64 Encoded): {}", signature);

            // 3. Base64 인코딩 및 특수 문자 교체
            String urlSafePolicy = makeUrlSafeBase64(policyJson.getBytes(StandardCharsets.UTF_8));
            String urlSafeSignature = makeUrlSafeBase64(signature.getBytes(StandardCharsets.UTF_8));

            log.info("URL Safe Policy: {}", urlSafePolicy);
            log.info("URL Safe Signature: {}", urlSafeSignature);

            // 4. Set-Cookie 헤더 설정
            String cookieDomain = ".gyechunsik.site"; // 필요에 따라 변경
            long maxAgeInSeconds = 3600; // 1시간

            // CloudFront-Policy
            ResponseCookie policyCookie = ResponseCookie.from("CloudFront-Policy", urlSafePolicy)
                    .domain(cookieDomain)
                    .path("/")
                    .secure(true)
                    .httpOnly(true)
                    .sameSite("None")
                    .maxAge(maxAgeInSeconds)
                    .build();

            // CloudFront-Signature
            ResponseCookie signatureCookie = ResponseCookie.from("CloudFront-Signature", urlSafeSignature)
                    .domain(cookieDomain)
                    .path("/")
                    .secure(true)
                    .httpOnly(true)
                    .sameSite("None")
                    .maxAge(maxAgeInSeconds)
                    .build();

            // CloudFront-Key-Pair-Id
            ResponseCookie keyPairIdCookie = ResponseCookie.from("CloudFront-Key-Pair-Id", keyPairId)
                    .domain(cookieDomain)
                    .path("/")
                    .secure(true)
                    .httpOnly(true)
                    .sameSite("None")
                    .maxAge(maxAgeInSeconds)
                    .build();

            // 응답에 쿠키 추가
            response.addHeader(HttpHeaders.SET_COOKIE, policyCookie.toString());
            response.addHeader(HttpHeaders.SET_COOKIE, signatureCookie.toString());
            response.addHeader(HttpHeaders.SET_COOKIE, keyPairIdCookie.toString());

            log.info("Custom Signed Cookies have been set in the response.");
        } catch (Exception e) {
            log.error("Failed to create custom signed cookies", e);
            throw new RuntimeException("Failed to create custom signed cookies", e);
        }
    }

    /**
     * Base64 인코딩 후 특수 문자 교체
     */
    private String makeUrlSafeBase64(byte[] data) {
        String base64 = Base64.getEncoder().encodeToString(data);
        // AWS 문서에 따라 특수 문자 교체
        return base64.replace("+", "-")
                .replace("=", "_")
                .replace("/", "~");
    }

    /**
     * Custom Policy JSON 생성
     */
    private String generatePolicyJson(String resourceUrl, long epochTime) {
        String json =
                """
                {"Statement":[{"Resource":"https://static.gyechunsik.site/*","Condition":{"DateLessThan":{"AWS:EpochTime":
                """
                + epochTime
                + "}}}]}";
        return json;
        // {"Statement":[{"Resource":"https://static.gyechunsik.site/*","Condition":{"DateLessThan":{"AWS:EpochTime":1734872081}}}]}
        // Map<String, Object> policy = Map.of(
        //         "Statement", List.of(
        //                 Map.of(
        //                         "Resource", resourceUrl,
        //                         "Condition", Map.of(
        //                                 "DateLessThan", Map.of(
        //                                         "AWS:EpochTime", epochTime
        //                                 )
        //                         )
        //                 )
        //         )
        // );
        //
        // try {
        //     String json = objectMapper.writeValueAsString(policy);
        //     // 공백 제거
        //     return json.replaceAll("\\s", "");
        // } catch (Exception e) {
        //     log.error("Failed to generate policy JSON", e);
        //     throw new RuntimeException("Failed to generate policy JSON", e);
        // }
    }

    /**
     * Policy 서명(Sign) 생성
     */
    private String signPolicy(String policy, PrivateKey privateKey) {
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(policy.getBytes(StandardCharsets.UTF_8));
            byte[] signedBytes = signature.sign();
            // 서명된 데이터를 Base64로 인코딩
            return Base64.getEncoder().encodeToString(signedBytes);
        } catch (Exception e) {
            log.error("Failed to sign policy", e);
            throw new RuntimeException("Failed to sign policy", e);
        }
    }

    // ----------------------

    /**
     * Canned Policy 기반의 서명된 쿠키 생성
     */
    public CookiesForCannedPolicy createSignedCookiesForCannedPolicy() {
        try {
            // 1. Private Key 로드
            PrivateKey privateKey = loadPrivateKey();

            // 2. Canned Policy 생성
            String resourceUrl = "https://" + cloudfrontDomain + "/chuncity/admin/*"; // 정확한 리소스 경로 포함
            Instant expirationDate = Instant.now().plusSeconds(3600); // 1시간 유효
            String cannedPolicy = buildCannedPolicy(resourceUrl, expirationDate);
            log.info("Canned Policy JSON: {}", cannedPolicy);

            // 3. CannedSignerRequest 생성
            CannedSignerRequest cannedSignerRequest = CannedSignerRequest.builder()
                    .resourceUrl(resourceUrl)
                    .privateKey(Paths.get(privateKeyResource.getFile().getPath()))
                    .keyPairId(keyPairId)
                    .expirationDate(expirationDate)
                    .build();

            // 4. 서명된 쿠키 생성
            CookiesForCannedPolicy signedCookies = cloudFrontUtilities.getCookiesForCannedPolicy(cannedSignerRequest);
            log.info("Generated Signed Cookies: expiresHeader=[{}], Signature=[{}], KeyPairId=[{}]",
                    signedCookies.expiresHeaderValue(),
                    signedCookies.signatureHeaderValue(),
                    signedCookies.keyPairIdHeaderValue());

            return signedCookies;
        } catch (IOException e) {
            log.error("Failed to load private key file", e);
            throw new RuntimeException("Failed to load private key file", e);
        } catch (Exception e) {
            log.error("Failed to create signed cookies", e);
            throw new RuntimeException("Failed to create signed cookies", e);
        }
    }

    /**
     * Canned Policy JSON 생성
     */
    private String buildCannedPolicy(String resourceUrl, Instant expirationDate) {
        Map<String, Object> policy = Map.of(
                "Statement", List.of(
                        Map.of(
                                "Resource", resourceUrl,
                                "Condition", Map.of(
                                        "DateLessThan", Map.of(
                                                "AWS:EpochTime", expirationDate.getEpochSecond()
                                        )
                                )
                        )
                )
        );

        try {
            String json = objectMapper.writeValueAsString(policy);
            // 공백 제거
            return json.replaceAll("\\s", "");
        } catch (Exception e) {
            log.error("Failed to generate canned policy JSON", e);
            throw new RuntimeException("Failed to generate canned policy JSON", e);
        }
    }

    // ----------------------

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

    /**
     * Admin 리소스 전체에 접근할 수 있는 Signed Cookies 생성
     */
    public CookiesForCannedPolicy createSignedCookies() {
        try {
            String privateKeyPath = privateKeyResource.getFile().getPath();
            log.info("Private Key Path: {}", privateKeyPath);

            CannedSignerRequest cannedSignerRequest = CannedSignerRequest.builder()
                    .resourceUrl(cloudfrontDomain) // 모든 경로에 대해 유효
                    .privateKey(Paths.get(privateKeyPath))
                    .keyPairId(keyPairId)
                    .expirationDate(Instant.now().plusSeconds(3600)) // 1시간
                    .build();

            log.info("cannedSignerRequest: {}", cannedSignerRequest.resourceUrl());
            log.info("cannedSignerRequest: {}", cannedSignerRequest.expirationDate());
            log.info("cannedSignerRequest: {}", cannedSignerRequest.keyPairId());
            log.info("cannedSignerRequest: {}", cannedSignerRequest.privateKey());

            CookiesForCannedPolicy signedCookies = cloudFrontUtilities.getCookiesForCannedPolicy(cannedSignerRequest);
            log.info("Generated Signed Cookies: expiresHeader=[{}], Signature=[{}], KeyPairId=[{}]",
                    signedCookies.expiresHeaderValue(),
                    signedCookies.signatureHeaderValue(),
                    signedCookies.keyPairIdHeaderValue());
            return signedCookies;
        } catch (IOException e) {
            log.error("Failed to load private key file", e);
            throw new RuntimeException("Failed to load private key file", e);
        } catch (Exception e) {
            log.error("Failed to create signed cookies", e);
            throw new RuntimeException("Failed to create signed cookies", e);
        }
    }

    public String generateSignedUrlForFile(String filePath) {
        try {
            Instant expirationDate = Instant.now().plusSeconds(300); // 5분 뒤 만료
            String privateKeyPath = privateKeyResource.getFile().getPath();
            log.info("Private Key Path: {}", privateKeyPath);

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
            log.error("Failed to generate signed URL for file: {}", filePath, e);
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
     * ADMIN index page 에 대한 Signed URL 생성
     */
    public String generateSignedUrlForIndexHtml() {
        try {
            CloudFrontUtilities cloudFrontUtilities = CloudFrontUtilities.create();
            Instant expirationDate = Instant.now().plusSeconds(300); // 5분 뒤 만료
            String privateKeyPath = privateKeyResource.getFile().getPath();
            log.info("Private Key Path: {}", privateKeyPath);

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
