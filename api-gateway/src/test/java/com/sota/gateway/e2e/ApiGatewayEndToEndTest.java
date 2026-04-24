package com.sota.gateway.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@Tag("e2e")
class ApiGatewayEndToEndTest {

    private static final String BASE_URL = "http://localhost:8080";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Test
    void healthAndMetricsShouldBeReachableWhenFullStackIsRunning() throws Exception {
        assumeTrue(isStackReachable(), "Docker Compose stack must be running for e2e tests");

        HttpResponse<String> health = send(HttpRequest.newBuilder(URI.create(BASE_URL + "/health")).GET().build());
        HttpResponse<String> metrics = send(HttpRequest.newBuilder(URI.create(BASE_URL + "/metrics")).GET().build());

        assertEquals(200, health.statusCode());
        assertEquals(200, metrics.statusCode());
        assertEquals("api-gateway", OBJECT_MAPPER.readTree(health.body()).get("service").asText());
        assertTrue(metrics.body().contains("http_server_requests"));
    }

    @Test
    void gatewayUserJourneyShouldExposeSagaAndPaymentEvents() throws Exception {
        assumeTrue(isStackReachable(), "Docker Compose stack must be running for e2e tests");

        String stamp = String.valueOf(Instant.now().toEpochMilli());
        JsonNode registerBody = postWithoutBody(BASE_URL + "/api/users/register?name=E2EUser&email=e2e" + stamp + "@test.com&password=E2Euser1234");
        String userId = registerBody.get("id").asText();

        JsonNode loginBody = postWithoutBody(BASE_URL + "/api/auth/login?userId=" + userId);
        String token = loginBody.get("token").asText();

        HttpResponse<String> createDelivery = send(authenticatedJsonPost("/api/deliveries", token, """
                {
                  "userId": "%s",
                  "pickupLocationLat": 48.8566,
                  "pickupLocationLon": 2.3522,
                  "dropoffLocationLat": 48.8647,
                  "dropoffLocationLon": 2.3490,
                  "weight": 2.1,
                  "requestedTimeStart": "2026-04-21T14:00:00Z",
                  "requestedTimeEnd": "2026-04-21T16:00:00Z"
                }
                """.formatted(userId)));
        assertEquals(200, createDelivery.statusCode());
        String deliveryId = stripJsonString(createDelivery.body());

        HttpResponse<String> startDelivery = send(authenticatedPost("/api/deliveries/" + deliveryId + "/start", token));
        assertEquals(200, startDelivery.statusCode());

        JsonNode saga = authenticatedGetJson("/api/deliveries/" + deliveryId + "/saga", token);
        JsonNode paymentEvents = authenticatedGetJson("/api/payments/" + userId + "_" + deliveryId + "/events", token);

        assertEquals(deliveryId, saga.get("deliveryId").asText());
        assertTrue(Set.of("PAYMENT_REQUESTED", "PAYMENT_CONFIRMED", "DRONE_ASSIGNMENT_REQUESTED", "WAITING_FOR_PICKUP", "IN_PROGRESS", "COMPLETED")
                .contains(saga.get("state").asText()));
        assertTrue(paymentEvents.isArray());
        assertTrue(paymentEvents.size() >= 1);
        assertEquals(userId + "_" + deliveryId, paymentEvents.get(0).get("paymentId").asText());
        assertEquals(deliveryId, paymentEvents.get(0).get("deliveryId").asText());
    }

    private static boolean isStackReachable() {
        try {
            HttpResponse<String> response = send(HttpRequest.newBuilder(URI.create(BASE_URL + "/health"))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build());
            return response.statusCode() == 200;
        } catch (Exception ignored) {
            return false;
        }
    }

    private static JsonNode postWithoutBody(String url) throws Exception {
        HttpResponse<String> response = send(HttpRequest.newBuilder(URI.create(url))
                .POST(HttpRequest.BodyPublishers.noBody())
                .header("Accept", "application/json")
                .build());
        assertEquals(200, response.statusCode());
        return OBJECT_MAPPER.readTree(response.body());
    }

    private static JsonNode authenticatedGetJson(String path, String token) throws Exception {
        HttpResponse<String> response = send(HttpRequest.newBuilder(URI.create(BASE_URL + path))
                .GET()
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/json")
                .build());
        assertEquals(200, response.statusCode());
        return OBJECT_MAPPER.readTree(response.body());
    }

    private static HttpRequest authenticatedPost(String path, String token) {
        return HttpRequest.newBuilder(URI.create(BASE_URL + path))
                .POST(HttpRequest.BodyPublishers.noBody())
                .header("Authorization", "Bearer " + token)
                .build();
    }

    private static HttpRequest authenticatedJsonPost(String path, String token, String body) {
        return HttpRequest.newBuilder(URI.create(BASE_URL + path))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .build();
    }

    private static HttpResponse<String> send(HttpRequest request) throws IOException, InterruptedException {
        return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static String stripJsonString(String body) {
        if (body != null && body.length() >= 2 && body.startsWith("\"") && body.endsWith("\"")) {
            return body.substring(1, body.length() - 1);
        }
        return body;
    }
}
