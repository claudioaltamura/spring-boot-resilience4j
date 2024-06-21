package de.claudioaltamura.spring.boot.resilience4j;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@WireMockTest
class ApplicationControllerIntegrationTest {

    @RegisterExtension
    static WireMockExtension wireMockServer = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("swapiConnector.baseURL", wireMockServer::baseUrl);
    }

    @LocalServerPort
    private int port;

    @AfterEach
    void resetAll() {
        wireMockServer.resetAll();
    }

    @Test
    void test() {
        wireMockServer.stubFor(
                get(urlPathMatching("/api/people/.*"))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBodyFile("swapi/response-200.json")
                        )
        );

        var restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();

        var people = restClient.get()
                .uri("/people/{id}", 2)
                .retrieve()
                .body(JsonNode.class);

        assertThat(people).isNotNull();
        assertThat(people.findValue("name").asText("empty")).isEqualTo("C-3PO");

        wireMockServer.verify(1, getRequestedFor(urlPathMatching("/api/people/.*")));
    }

}
