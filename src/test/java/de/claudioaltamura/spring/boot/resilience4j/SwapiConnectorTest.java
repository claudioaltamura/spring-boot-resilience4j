package de.claudioaltamura.spring.boot.resilience4j;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SwapiConnectorTest {

    private SwapiConnector swapiConnector;

    @BeforeEach
    public void setUp() {
        swapiConnector = new SwapiConnector(new SwapiConfiguration().restClient());
    }

    @Test
    @DisplayName("get people")
    void shouldReturnPeople() {
        var people = swapiConnector.people(2);

        assertThat(people.findValue("name").asText("empty")).isEqualTo("C-3PO");
    }

}