package com.hsrOptimiser.client;

import com.hsrOptimiser.domain.projectYatta.ProjectYattaResponse;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

@Component
public class ProjectYattaClient {

    @Value("${project-yatta.max-retries}")
    int maxRetries;
    @Value("${project-yatta.retry-interval-second}")
    int retryInterval;

    @Autowired
    WebClient projectYattaWebClient;

    @Cacheable(value = "characterCache", key = "#characterId")
    public Flux<ProjectYattaResponse> getCharacterDataAsync(String characterId) {
        return fetchData("/avatar/" + characterId);
    }

    @Cacheable(value = "lightConeCache", key = "#lightConeId")
    public Flux<ProjectYattaResponse> getLightConeDataAsync(String lightConeId) {
        return fetchData("/equipment/" + lightConeId);
    }

    private Flux<ProjectYattaResponse> fetchData(String uri) {
        return projectYattaWebClient.get()
            .uri(uri)
            .retrieve()
            .bodyToFlux(ProjectYattaResponse.class)
            .retryWhen(getRetryPolicy())
            .onErrorResume(WebClientResponseException.class, this::handleError);
    }

    private Retry getRetryPolicy() {
        return Retry.fixedDelay(maxRetries, Duration.ofSeconds(retryInterval))
            .filter(throwable -> throwable instanceof WebClientResponseException &&
                ((WebClientResponseException) throwable).getStatusCode().value() == 503);
    }

    private Flux<ProjectYattaResponse> handleError(WebClientResponseException e) {
        System.err.println("WebClient error: " + e.getMessage());
        return Flux.empty(); // Return an empty Flux of the correct type
    }
}
