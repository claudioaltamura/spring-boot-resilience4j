package de.claudioaltamura.spring.boot.resilience4j;

public class SwapiConnectorException extends RuntimeException {

    public SwapiConnectorException(String errorMessage) {
        super(errorMessage);
    }

}
