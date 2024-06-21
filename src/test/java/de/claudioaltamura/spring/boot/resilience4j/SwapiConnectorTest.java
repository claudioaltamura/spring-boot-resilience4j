package de.claudioaltamura.spring.boot.resilience4j;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SwapiConnectorTest {

    private SwapiConnector swapiConnector;

    @BeforeEach
    public void setUp() {
        swapiConnector = new SwapiConnector(
                new SwapiConnectorConfiguration("https://swapi.dev")
                        .restClient()
        );
    }

    @Test
    @DisplayName("get people")
    void shouldReturnPeople() {
        var people = swapiConnector.getPeople(2);

        assertThat(people.getName()).isEqualTo("C-3PO");
    }

}