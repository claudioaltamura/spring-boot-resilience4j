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

import java.util.stream.IntStream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

class ApplicationControllerIntegrationTest extends AbstractResilience4JIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationControllerIntegrationTest.class);

    @Test
    @DisplayName("return succesfully People")
    void shouldReturnSuccesfullyPeople() {
        stubSuccessfulRequestUrlPathMatching("/api/people/.*", "swapi/response-200.json");

        var restClient = getRestClient();
        var people = restClient.get()
                .uri("/people/{id}", 2)
                .retrieve()
                .body(JsonNode.class);

        assertThat(people).isNotNull();
        assertThat(people.findValue("name").asText("empty")).isEqualTo("C-3PO");

        wireMockServer.verify(1, getRequestedFor(urlPathMatching("/api/people/.*")));
    }

    private void stubSuccessfulRequestUrlPathMatching(String urlPathMatching, String file) {
        wireMockServer.stubFor(
                get(urlPathMatching(urlPathMatching))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBodyFile(file)
                        )
        );
    }

    @Test
    @DisplayName("should open circuit breaker")
    void shouldOpenCircuitBreaker() {
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
                            .onStatus(HttpStatusCode::is5xxServerError, (req, resp) ->
                                logger.error("{} {}", resp.getStatusText(), resp.getStatusCode())
                            )
                            .toEntity(JsonNode.class);
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                });

        //Then
        assertCircuitBreakerState(SwapiConnector.PEOPLE_ENDPOINT_CIRCUIT_BREAKER, CircuitBreaker.State.OPEN);
    }

    @Test
    @DisplayName("should close circuit breaker")
    void shouldCloseCircuitBreaker() {
        //Given
        transitionToOpenState(SwapiConnector.PEOPLE_ENDPOINT_CIRCUIT_BREAKER);
        circuitBreakerRegistry.circuitBreaker(SwapiConnector.PEOPLE_ENDPOINT_CIRCUIT_BREAKER).transitionToHalfOpenState();
        stubSuccessfulRequestUrlPathMatching("/api/people/.*", "swapi/response-200.json");

        //When
        var restClient = getRestClient();
        IntStream.rangeClosed(1, 3)
                .forEach(i -> {
                    ResponseEntity<JsonNode> response = restClient.get()
                            .uri("/people/{id}", 2)
                            .retrieve()
                            .onStatus(HttpStatusCode::is5xxServerError, (req, resp) ->
                                logger.error("{} {}", resp.getStatusText(), resp.getStatusCode())
                            )
                            .toEntity(JsonNode.class);
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                });

        //Then
        assertCircuitBreakerState(SwapiConnector.PEOPLE_ENDPOINT_CIRCUIT_BREAKER, CircuitBreaker.State.CLOSED);
    }
}
