package com.footballay.core.domain.token;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class RemoteHostTokenService {

    private final SecretKey SECRET_KEY; // provider 에 의해 주입받음

    public RemoteHostTokenService(HS256KeyProvider provider) {
        SECRET_KEY = provider.getSECRET_KEY();
    }

    public String generateRemoteHostToken(String remoteCode, LocalDateTime generatedTime) {
        return generateToken(remoteCode, generatedTime);
    }

    public boolean validateRemoteHostToken(String token, String remoteCode) {
        try {
            Jws<Claims> claimsJws = getClaimsFromToken(token);
            return isRemoteHostToken(claimsJws) && containMatchRemoteCode(claimsJws, remoteCode);
        } catch (JwtException | IllegalArgumentException e) {
            log.info("Token validation fail", e);
            return false;
        } catch (Exception e) {
            log.error("Unexpected Token error", e);
            return false;
        }
    }

    private String generateToken(String remoteCode, LocalDateTime generatedTime) {
        Map<String, Object> claims = createClaimsForRemoteHost(remoteCode);
        Date issuedDate = convertIssuedAt(generatedTime);

        return Jwts.builder()
                .subject("remoteHost")
                .claims(claims)
                .issuedAt(issuedDate)
                .signWith(SECRET_KEY)
                .compact();
    }

    private Map<String, Object> createClaimsForRemoteHost(String remoteCode) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("remoteCode", remoteCode);
        claims.put("type", "remoteHost");
        return claims;
    }

    private boolean isRemoteHostToken(Jws<Claims> claimsJws) {
        return claimsJws.getPayload().get("type", String.class).equals("remoteHost");
    }

    private boolean containMatchRemoteCode(Jws<Claims> claimsJws, String remoteCode) {
        return claimsJws.getPayload().get("remoteCode", String.class).equals(remoteCode);
    }

    private Jws<Claims> getClaimsFromToken(String token) {
        return Jwts.parser().verifyWith(SECRET_KEY).build().parseSignedClaims(token);
    }

    private Date convertIssuedAt(LocalDateTime generatedTime) {
        return Date.from(generatedTime.atZone(ZoneId.of("UTC")).toInstant());
    }
}
