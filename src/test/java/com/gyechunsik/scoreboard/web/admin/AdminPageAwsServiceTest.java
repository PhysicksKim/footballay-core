package com.gyechunsik.scoreboard.web.admin;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@ActiveProfiles({"mockapi","dev","aws"})
class AdminPageAwsServiceTest {

    @Autowired
    private AdminPageAwsService adminPageAwsService;

    private final String publicKeyString = """
        -----BEGIN PUBLIC KEY-----
        MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAu8oW+LfirnLpIYl9+XqF
        5l021Hx4HCCS2qK5g5hslMWx6W5JHEJmpKcUmcULcJ3CFGgTIcxfUupq1fwGXI/n
        bEEFfxirbjq55Igs2ejy0d1PBa7zx60TR37jntuFp5pc0Y5O12Y7yr+YpbioMZXU
        /6+KwP7IdeS6zP5ay2RTH2B7ilJuVlhYQjdCOMXkxo/ZTfnzpTh7cluLVKK5gG6k
        eWlxr7khak8Sn08gYUvkP/BNZr3OM8HEO8rJ2AxQuWnZjhr8awKAvBTt/uq+j8bt
        LkLCCzTtPUo6KXZ5zkD2esvtCvwWvFdjqQjs8Ivi3lqUUsqzcSzCQEWpsR40NNK7
        3wIDAQAB
        -----END PUBLIC KEY-----
        """;

    @Test
    public void signatureVerifyTest() throws Exception {
        // given
        String encodedIssuetoken = "{%22issuer%22:%22physickskim%22,%22expiresAt%22:1734776900," +
                "%22purpose%22:%22issue-chuncity-admin-cloudfront-cookie%22," +
                "%22redirect%22:%22https://gyechunsik.site/admin%22}";
        String signature = "EF8LF9IGCUWhvsTZKrbN+zt0T0kftNm0FIXloK88ekSJYTOn768d66nnhR7wBHfP3MShAxg8a5e" +
                "US+XcWibVRcvv7Nye5OLmoWcKoFah8Ds/K/Z1Z7otZIQDPBXayjGnBJ17iTEwSy1o+QGN6aQMGZ+2KKOVYAwbY" +
                "f5RJjAU1jgXKB8Ii8DZ0eVd/GsfeS1R9UfSIwLi4Y0ZG76BterbKITBOB3GAte+KSWBc2fNpnkBuK1RZBx33iJ" +
                "8UT1F3hVv63LHPOhUNjqPkMAJR7YDDoIlvc4o8qUB5ZO7CYmbOlObfGtMbv1pJTptjmQiQjjqF5Qi1tWhH5cc9" +
                "NjhEjxOyQ==";

        String decodedIssuetoken = URLDecoder.decode(encodedIssuetoken, StandardCharsets.UTF_8);

        // when
        boolean isValid = verifySignature(decodedIssuetoken, signature);

        // then
        assertTrue(isValid, "The signature should be valid.");
    }

    private boolean verifySignature(String data, String signature) {
        try {
            // Remove PEM headers and decode Base64 public key
            String publicKeyPem = publicKeyString
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyPem);

            // Convert the public key bytes to PublicKey instance
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(keySpec);

            // Initialize signature verifier
            Signature rsaSignature = Signature.getInstance("SHA256withRSA");
            rsaSignature.initVerify(publicKey);
            rsaSignature.update(data.getBytes(StandardCharsets.UTF_8));

            // Verify the signature
            return rsaSignature.verify(Base64.getDecoder().decode(signature));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}