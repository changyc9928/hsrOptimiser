package com.hsrOptimiser.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hsrOptimiser.DTO.asagi.MocRequest;
import com.hsrOptimiser.DTO.asagi.MocResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class AsagiClient {

    @Autowired
    WebClient asagiWebClient;

    public MocResponse calculateDamage(MocRequest request) {
        return asagiWebClient.post().uri("/api/calc")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .onStatus(HttpStatusCode::isError, clientResponse -> {
                HttpStatusCode status = clientResponse.statusCode();
                MediaType contentType = clientResponse.headers().contentType().orElse(null);

                // If it's HTML (like Cloudflare's 504/502), do not read the giant body
                if (contentType != null && contentType.includes(MediaType.TEXT_HTML)) {
                    String htmlError = String.format("HTTP %s %s (HTML Response intercepted)",
                        status.value(), status.toString());
                    System.err.println("Server rejected request: " + htmlError);
                    return Mono.error(new RuntimeException("API Error: " + htmlError));
                }

                // Otherwise, it's a standard JSON error payload from the actual app server
                return clientResponse.bodyToMono(String.class).flatMap(errorBody -> {
                    // Truncate if the body somehow still turns out massive
                    String loggedError =
                        errorBody.length() > 200 ? errorBody.substring(0, 197) + "..." : errorBody;
                    System.err.println("Server rejected request: " + loggedError);
                    return Mono.error(new RuntimeException("API Error: " + errorBody));
                });
            })
            .bodyToMono(MocResponse.class)
            .block();
    }
}
