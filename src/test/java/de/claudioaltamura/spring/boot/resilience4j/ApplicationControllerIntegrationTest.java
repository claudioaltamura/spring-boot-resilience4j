package de.claudioaltamura.spring.boot.resilience4j;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

class ApplicationControllerIntegrationTest extends AbstractResilience4JIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationControllerIntegrationTest.class);

    @Test
    @DisplayName("return succesfully People")
    void shouldReturnSuccesfullyPeople() {
        wireMockServer.stubFor(
                get(urlPathMatching("/api/people/.*"))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBodyFile("swapi/response-200.json")
                        )
        );

        var restClient = getRestClient();
        var people = restClient.get()
                .uri("/people/{id}", 2)
                .retrieve()
                .body(JsonNode.class);

        assertThat(people).isNotNull();
        assertThat(people.findValue("name").asText("empty")).isEqualTo("C-3PO");

        wireMockServer.verify(1, getRequestedFor(urlPathMatching("/api/people/.*")));
    }

    @Test
    @DisplayName("set circuit breaker in open state and back")
    void shouldSetCircuitBreakerInOpenStateAndBack() {
        //Given
        wireMockServer.stubFor(
                get(urlPathMatching("/api/people/.*"))
                        .willReturn(serverError())
        );
        var restClient = getRestClient();
        //When first calls failed
        IntStream.rangeClosed(1, 5)
                .forEach(i -> {
                    ResponseEntity<JsonNode> response = restClient.get()
                            .uri("/people/{id}", 2)
                            .retrieve()
                             .onStatus(HttpStatusCode::is5xxServerError, (req, resp) -> {
                                logger.error("{} {}", resp.getStatusText(), resp.getStatusCode());
                             })
                            .toEntity(JsonNode.class);
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                });

        wireMockServer.resetRequests();
        wireMockServer.getStubMappings().getFirst().setResponse(aResponse()
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBodyFile("swapi/response-200.json").build());
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }

        IntStream.rangeClosed(1, 10)
                .forEach(i -> {
                    ResponseEntity<JsonNode> response = restClient.get()
                            .uri("/people/{id}", 2)
                            .retrieve()
                            .onStatus(HttpStatusCode::is5xxServerError, (req, resp) -> {
                                logger.error("{} {}", resp.getStatusText(), resp.getStatusCode());
                            })
                            .toEntity(JsonNode.class);
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                });
        assertCircuitBreakerState(SwapiConnector.PEOPLE_ENDPOINT_CIRCUIT_BREAKER, CircuitBreaker.State.CLOSED);
    }

}
