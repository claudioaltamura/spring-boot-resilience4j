package de.claudioaltamura.spring.boot.resilience4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@Configuration
public class SwapiConnectorConfiguration {

    @Value("${swapiConnector.baseURL}")
    private String baseURL;

    public SwapiConnectorConfiguration() {}

    public SwapiConnectorConfiguration(String baseURL) {
        this.baseURL = baseURL;
    }

    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                .baseUrl(baseURL)
                .defaultHeaders(headers-> headers.add("Accept", MediaType.APPLICATION_JSON_VALUE))
                .build();
    }

}
