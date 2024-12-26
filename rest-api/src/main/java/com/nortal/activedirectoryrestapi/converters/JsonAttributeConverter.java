package com.nortal.activedirectoryrestapi.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Converter
public class JsonAttributeConverter implements AttributeConverter<JsonNode, String> {

    private final Logger logger = LoggerFactory.getLogger(JsonAttributeConverter.class);

    @Override
    public String convertToDatabaseColumn(JsonNode attribute) {
        return attribute != null ? attribute.toPrettyString() : null;
    }

    @Override
    public JsonNode convertToEntityAttribute(String dbData) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readTree(dbData);
        } catch (JsonProcessingException e) {
            String errorMessage = "Error converting to JSON object";
            logger.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }
}
