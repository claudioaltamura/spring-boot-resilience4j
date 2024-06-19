package de.claudioaltamura.spring.boot.resilience4j;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class SwapiConfiguration {

    private static final String BASE_URL =  "https://swapi.dev";

    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                .baseUrl(BASE_URL)
                .build();
    }
}
