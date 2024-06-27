package de.claudioaltamura.spring.boot.resilience4j;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class SwapiConnector {

    static final String PEOPLE_ENDPOINT_CIRCUIT_BREAKER = "peopleEndpointCircuitBreaker";

    private final RestClient restClient;

    @Autowired
    public SwapiConnector(RestClient restClient) {
        this.restClient = restClient;
    }

    @CircuitBreaker(name = "peopleEndpointCircuitBreaker")
    public JsonNode getPeople(int id) {
        return restClient.get()
                .uri("/api/people/{id}/?format=json", id)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(JsonNode.class);
    }

    public JsonNode getPeopleWithDelay(int id) {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return getPeople(id);
    }
}
