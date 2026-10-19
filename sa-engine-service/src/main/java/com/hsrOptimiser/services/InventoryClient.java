package com.hsrOptimiser.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hsrOptimiser.DTO.hsrScanner.ScannedData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class InventoryClient {

    private final WebClient inventoryWebClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ScannedData getScannedData(String bearerToken) {
        String body = inventoryWebClient.get()
            .uri("/data/scanned")
            .header("Authorization", "Bearer " + bearerToken)
            .retrieve()
            .bodyToMono(String.class)
            .block();
        try {
            JsonNode node = objectMapper.readTree(body);
            JsonNode dataNode = node.get("data");
            return objectMapper.treeToValue(dataNode, ScannedData.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse scanned data from inventory service", e);
        }
    }
}
