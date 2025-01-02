package com.nortal.activedirectoryrestapi.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nortal.activedirectoryrestapi.exceptions.TerminatingError;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class JsonAttributeConverter implements AttributeConverter<JsonNode, String> {

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
            throw new TerminatingError("Error converting to JSON object", e);
        }
    }
}
