package de.claudioaltamura.spring.boot.resilience4j;

import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;

import java.util.List;

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

    private static boolean isStubActiveForUrl(List<StubMapping> stubMappings, String url, RequestMethod method) {
        return stubMappings.stream().anyMatch(mapping ->
                mapping.getRequest().getUrlMatcher().match(url).isExactMatch() &&
                        mapping.getRequest().getMethod() == method
        );
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

        List<StubMapping> allStubMappings = wireMockServer.listAllStubMappings().getMappings();
        String urlToCheck = "/api/people/2";
        assertThat(isStubActiveForUrl(allStubMappings, urlToCheck, RequestMethod.GET)).isTrue();

        var restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .defaultHeaders(header->{ header.add("Accept", MediaType.APPLICATION_JSON_VALUE);})
                .build();

        var people = restClient.get()
                .uri("/people/{id}", 2)
                .retrieve()
                .body(People.class);

        assertThat(people.getName()).isEqualTo("C-3PO");

        wireMockServer.verify(getRequestedFor(urlPathMatching("/api/people/.*")));
    }

}
