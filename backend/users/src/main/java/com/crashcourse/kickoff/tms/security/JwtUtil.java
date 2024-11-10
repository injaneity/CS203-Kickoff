package com.crashcourse.kickoff.tms.security;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.crashcourse.kickoff.tms.user.model.User;

import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
    private Dotenv dotenv;

    // Try to load JWT_SECRET_KEY from the system environment, fallback to dotenv if
    // system env is null
    private String JWT_SECRET_KEY;

    public static final String ROLES_CLAIM = "roles";
    public static final String USERID_CLAIM = "userId";

    public JwtUtil() {
        JWT_SECRET_KEY = System.getenv("JWT_SECRET_KEY");

        // If JWT_SECRET_KEY is null, load from dotenv
        if (JWT_SECRET_KEY == null) {
            dotenv = Dotenv.load();
            JWT_SECRET_KEY = dotenv.get("JWT_SECRET_KEY"); // Load from dotenv if system env is null
        }
    }

    private final long jwtExpirationInMillis = 3600000; // 1 hour in milliseconds

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(JWT_SECRET_KEY); // Decode the Base64-encoded secret key
        return Keys.hmacShaKeyFor(keyBytes); // Generate the SecretKey using the decoded bytes
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get(USERID_CLAIM, Long.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public List<String> extractRoles(String token) {
        List<?> roles = extractClaim(token, claims -> claims.get(ROLES_CLAIM, List.class));
        return roles != null ? roles.stream().map(String.class::cast).collect(Collectors.toList()) : null;
    }

    private Claims extractAllClaims(String token) {
        SecretKey key = getSigningKey();

        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new ExpiredJwtException(e.getHeader(), e.getClaims(), "Token has expired");
        } catch (Exception e) {
            throw new RuntimeException("Invalid JWT token");
        }
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(USERID_CLAIM, user.getId()); // Add userId to the claims
        claims.put(ROLES_CLAIM, user.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList())); // Add roles to the claims

        return createToken(claims, user.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        SecretKey key = getSigningKey();

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationInMillis))
                .signWith(key, SignatureAlgorithm.HS256) // Use the Key with SignatureAlgorithm
                .compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
