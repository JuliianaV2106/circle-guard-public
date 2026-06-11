package com.circleguard.auth.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Component
public class IdentityClient {

    private static final Logger logger = LoggerFactory.getLogger(IdentityClient.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String IDENTITY_URL = "http://localhost:8083/api/v1/identities/map";

    @Retry(name = "identityService", fallbackMethod = "getAnonymousIdFallback")
    @CircuitBreaker(name = "identityService", fallbackMethod = "getAnonymousIdFallback")
    public UUID getAnonymousId(String realIdentity) {
        logger.info("Calling identity-service for user: {}", realIdentity);
        Map<String, String> request = Map.of("realIdentity", realIdentity);
        Map response = restTemplate.postForObject(IDENTITY_URL, request, Map.class);
        return UUID.fromString(response.get("anonymousId").toString());
    }

    public UUID getAnonymousIdFallback(String realIdentity, Exception ex) {
        logger.warn("Fallback after retries/circuit-breaker for identity-service. User: {}. Error: {}",
                realIdentity, ex.getMessage());
        return UUID.nameUUIDFromBytes(realIdentity.getBytes());
    }
}