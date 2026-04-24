package com.sota.gateway.infrastructure.proxy;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.Enumeration;
public abstract class BaseHttpProxy {

    protected final RestTemplate restTemplate;
    private final String baseUrl;

    protected BaseHttpProxy(RestTemplate restTemplate, String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public ResponseEntity<byte[]> forward(HttpMethod method,
                                          String downstreamPath,
                                          HttpServletRequest request,
                                          byte[] body) {
        String targetUrl = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path(normalizePath(downstreamPath))
                .query(request.getQueryString())
                .build()
                .toUriString();

        HttpHeaders headers = copyHeaders(request);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getName() != null) {
            headers.set("X-User-Id", authentication.getName());
        }

        HttpEntity<byte[]> entity = new HttpEntity<>(body, headers);
        ResponseEntity<byte[]> response = restTemplate.exchange(targetUrl, method, entity, byte[].class);

        return new ResponseEntity<>(response.getBody(), sanitizeResponseHeaders(response.getHeaders()), response.getStatusCode());
    }

    private String normalizePath(String downstreamPath) {
        if (downstreamPath == null || downstreamPath.isBlank()) {
            return "";
        }
        return downstreamPath.startsWith("/") ? downstreamPath : "/" + downstreamPath;
    }

    private HttpHeaders copyHeaders(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames != null && headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (HttpHeaders.HOST.equalsIgnoreCase(headerName)
                    || HttpHeaders.CONTENT_LENGTH.equalsIgnoreCase(headerName)
                    || HttpHeaders.CONNECTION.equalsIgnoreCase(headerName)
                    || HttpHeaders.TRANSFER_ENCODING.equalsIgnoreCase(headerName)
                    || HttpHeaders.AUTHORIZATION.equalsIgnoreCase(headerName)) {
                continue;
            }
            headers.put(headerName, Collections.list(request.getHeaders(headerName)));
        }
        return headers;
    }

    private HttpHeaders sanitizeResponseHeaders(HttpHeaders originalHeaders) {
        HttpHeaders headers = new HttpHeaders();
        headers.putAll(originalHeaders);
        headers.remove(HttpHeaders.TRANSFER_ENCODING);
        headers.remove(HttpHeaders.CONNECTION);
        headers.remove("Keep-Alive");
        return headers;
    }
}
