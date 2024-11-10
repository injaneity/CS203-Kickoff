package com.crashcourse.kickoff.tms.security;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtAuthService {

    private final JwtUtil jwtUtil;
    public static final String BEARER_PREFIX = "Bearer ";

    public ResponseEntity<String> validateToken(String token, Long user_id) {
        if (token == null || !token.startsWith(BEARER_PREFIX)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Authorization token is missing or invalid");
        }

        token = token.substring(7); // Remove "Bearer " prefix
        Long userIdFromToken = jwtUtil.extractUserId(token);

        if (!user_id.equals(userIdFromToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You are not authorized to perform this action");
        }
        return null; // Indicating token validation is successful
    }

    public ResponseEntity<String> validateAdminToken(String token) {
        if (token == null || !token.startsWith(BEARER_PREFIX)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Authorization token is missing or invalid");
        }

        token = token.substring(7); // Remove "Bearer " prefix
        List<String> roles = jwtUtil.extractRoles(token);

        if (roles == null || !roles.contains("ROLE_ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You are not authorized to perform this action");
        }
        return null; // Indicating token validation is successful
    }
}
