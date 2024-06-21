package de.claudioaltamura.spring.boot.resilience4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class SwapiConnector {

    private final RestClient restClient;

    @Autowired
    public SwapiConnector(RestClient restClient) {
        this.restClient = restClient;
    }

    public People getPeople(int id) {
        return restClient.get()
                .uri("/api/people/{id}/?format=json", id)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(People.class);
    }

    public People getPeopleWithDelay(int id) {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return getPeople(id);
    }
}
