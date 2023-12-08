package com.mastercom.client;

import com.mastercom.exception.AutoCompleteGoogleAPIException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@Slf4j
public class GoogleMapAPIHelper {
    private WebClient webClient;

    @Value("${googlemap.api.host}")
    private String googleMapHostURL;

    @Value("${googlemap.api.key}")
    private String googleMapAPIKey;

    @Value("${googlemap.api.autocomplete.path}")
    private String googleMapAutocompletePath;


    @PostConstruct
    public void setUp() {
        this.webClient = WebClient.builder()
                .baseUrl(googleMapHostURL)
                .defaultHeader("Content-Type", "application/json")
                .filter(logRequest())
                .build();
    }

    public Mono<ResponseEntity<Object>> autocomplete(String searchQuery, String correlationId) {
        log.debug("CorrelationId:, {}", correlationId);
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(googleMapAutocompletePath)
                        .queryParam("input", searchQuery)
                        .queryParam("key", googleMapAPIKey)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::is5xxServerError, response -> response.toEntity(Object.class).map(a -> new AutoCompleteGoogleAPIException(a, correlationId)))
                .onStatus(HttpStatusCode::is4xxClientError, response -> response.toEntity(Object.class).map(a -> new AutoCompleteGoogleAPIException(a, correlationId)))
                .toEntity(Object.class)
                .timeout(Duration.ofSeconds(10));
    }

    private ExchangeFilterFunction logRequest() {
        return (clientRequest, next) -> {
            log.info("GoogleMapAPIHelper >>> logRequest => {}", clientRequest.url());
            return next.exchange(clientRequest);
        };
    }
}
