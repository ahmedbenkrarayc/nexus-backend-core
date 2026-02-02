package com.nexus.shared.security.service;

import com.nexus.shared.security.model.NexusPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JwtValidationService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Parses and validates the JWT token, then extracts a NexusPrincipal from the claims.
     * Throws an exception if the token is invalid or expired.
     */
    public NexusPrincipal validateAndExtract(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String username = claims.getSubject();
        Number rawUserId = (Number) claims.get("userId");
        Long userId = rawUserId != null ? rawUserId.longValue() : null;

        @SuppressWarnings("unchecked")
        List<String> rawRoles = (List<String>) claims.get("roles");
        List<GrantedAuthority> authorities = rawRoles == null
                ? List.of()
                : rawRoles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        return new NexusPrincipal(userId, username, authorities);
    }
}
