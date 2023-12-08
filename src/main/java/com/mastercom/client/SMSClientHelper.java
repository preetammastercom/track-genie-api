package com.mastercom.client;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@Slf4j
public class SMSClientHelper {

    private WebClient webClient;
    private final static String OTP_MSG = "Welcome to the 6666 powered by SMSINDIAHUB. Your OTP for registration is 9999\n";

    @Value("${sms.api.host}")
    private String smsHostURL;

    @Value("${sms.api.sendsms.path}")
    private String sendSMSPath;

    @PostConstruct
    public void setUp() {
        this.webClient = WebClient.builder()
                .baseUrl(smsHostURL)
                .defaultHeader("Content-Type", "application/json")
                .filter(logRequest())
                .build();
    }
    public Mono<String> sendOtp(String mobileNumber, String otp) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(sendSMSPath)
                        .queryParam("APIKey","IRn6BNZvXEKDIR9CpnZNug")
                        .queryParam("msisdn",91+mobileNumber)
                        .queryParam("sid","SMSHUB")
                        .queryParam("msg",OTP_MSG)
                        .queryParam("fl","0")
                        .queryParam("gwid","2")
                .build())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> Mono.error(new RuntimeException("4xx error")))
                .onStatus(HttpStatusCode::is5xxServerError, response -> Mono.error(new RuntimeException("5xx error")))
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(10));
    }

    private ExchangeFilterFunction logRequest() {
        return (clientRequest, next) -> {
            log.info("SMSClientHelper >>> logRequest => {}",clientRequest.url());
            return next.exchange(clientRequest);
        };
    }
}
