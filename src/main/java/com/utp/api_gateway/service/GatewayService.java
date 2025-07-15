package com.utp.api_gateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class GatewayService {

    @Autowired
    private RestTemplate restTemplate;

    public ResponseEntity<?> forwardPost(String url, Map<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Object> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    Object.class
            );
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (HttpStatusCodeException e) {
            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(Map.of("error", e.getMessage(), "details", e.getResponseBodyAsString()));
        }
    }

    public ResponseEntity<?> forwardPostWithToken(String url, Map<String, Object> body, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", ensureBearer(token)); // ⬅️ incluimos el token

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Object> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    Object.class
            );
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (HttpStatusCodeException e) {
            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(Map.of("error", e.getMessage(), "details", e.getResponseBodyAsString()));
        }
    }

    public ResponseEntity<?> forwardPostWithHeaders(String url, Map<String, Object> body, HttpHeaders extraHeaders) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.addAll(extraHeaders);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        return restTemplate.postForEntity(url, request, Object.class);
    }

    public ResponseEntity<?> forwardGet(String url, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", ensureBearer(token));

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.GET, entity, Object.class);
    }

    public ResponseEntity<?> forwardDelete(String url, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", ensureBearer(token));

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.DELETE, entity, Object.class);
    }

    public Map<String, Object> validateToken(String token) {
        try {
            String url = "http://localhost:8081/auth/validate";

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", ensureBearer(token));

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            return response.getBody();
        } catch (Exception e) {
            return Map.of(
                    "valid", false,
                    "message", "Token inválido o error al validar",
                    "error", e.getMessage()
            );
        }
    }

    private String ensureBearer(String token) {
        return token != null && token.startsWith("Bearer ") ? token : "Bearer " + token;
    }
}


