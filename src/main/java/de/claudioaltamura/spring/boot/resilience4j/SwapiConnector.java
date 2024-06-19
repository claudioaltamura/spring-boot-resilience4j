package de.claudioaltamura.spring.boot.resilience4j;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class SwapiConnector {

    private final RestClient restClient;

    @Autowired
    public SwapiConnector(RestClient restClient) {
        this.restClient = restClient;
    }

    public JsonNode people(int id) {
        return restClient.get()
                .uri("/api/people/{id}/?format=json", id)
                .retrieve()
                .body(JsonNode.class);
    }

    public JsonNode peopleWithDelay(int id) {
        var people = people(id);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return people;
    }
}
