package com.sota.gateway.infrastructure.controller;

import com.sota.gateway.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtUtil jwtUtil;

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @PostMapping(value = "/login", params = "userId")
    public ResponseEntity<Map<String, String>> loginFromQuery(
            @RequestParam(required = false) String userId) {
        return buildTokenResponse(userId);
    }

    @PostMapping(value = "/login", consumes = "application/json")
    public ResponseEntity<Map<String, String>> login(
            @RequestBody(required = false) Map<String, String> body) {

        Map<String, String> payload = body != null ? body : Collections.emptyMap();
        return buildTokenResponse(payload.get("userId"));
    }

    private ResponseEntity<Map<String, String>> buildTokenResponse(String resolvedUserId) {
        if (resolvedUserId == null || resolvedUserId.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "userId required"));
        }

        String token = jwtUtil.generateToken(resolvedUserId);
        return ResponseEntity.ok(Map.of("token", token));
    }
}
