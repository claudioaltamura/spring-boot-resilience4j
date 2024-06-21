package de.claudioaltamura.spring.boot.resilience4j;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConversionException;

import java.io.IOException;

public class JsonNodeHttpMessageConverter extends AbstractHttpMessageConverter<JsonNode> {

    private final ObjectMapper objectMapper;

    public JsonNodeHttpMessageConverter(ObjectMapper objectMapper) {
        super(MediaType.APPLICATION_JSON);
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return JsonNode.class.isAssignableFrom(clazz);
    }

    @Override
    protected JsonNode readInternal(Class<? extends JsonNode> clazz, HttpInputMessage inputMessage)
            throws IOException, HttpMessageConversionException {
        return objectMapper.readTree(inputMessage.getBody());
    }

    @Override
    protected void writeInternal(JsonNode jsonNode, HttpOutputMessage outputMessage)
            throws IOException, HttpMessageConversionException {
        objectMapper.writeValue(outputMessage.getBody(), jsonNode);
    }
}
