package com.nortal.activedirectoryrestapi.components;

import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import org.springframework.boot.jackson.JsonComponent;

@JsonComponent
public class CustomJsonDeserializer extends JsonDeserializer<Map<String, Object>>{

    @Override
    public Map<String, Object> deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
        List<char[]> charArraysToErase = new ArrayList<>();
        Map<String, Object> result = parseObject(jsonParser, charArraysToErase);

        jsonParser.close();

        // Erase buffer possibly containing password data
        for (char[] buffer : charArraysToErase) {
            Arrays.fill(buffer, '\u0000');
        }

        return result;
    }

    private Map<String, Object> parseObject(JsonParser jp, List<char[]> charArraysToErase) throws IOException {
        Map<String, Object> map = new HashMap<>();
        JsonToken token;

        while ((token = jp.nextToken()) != JsonToken.END_OBJECT) {
            if (token == JsonToken.FIELD_NAME) {
                String fieldName = jp.currentName();
                jp.nextToken(); // Move to the value
                map.put(fieldName, parseValue(jp, charArraysToErase));
            }
        }
        return map;
    }

    private Object parseValue(JsonParser jp, List<char[]> charArraysToErase) throws IOException {
        JsonToken token = jp.currentToken();

        return switch (token) {
            case START_OBJECT -> parseObject(jp, charArraysToErase);
            case START_ARRAY -> parseArray(jp, charArraysToErase);
            case VALUE_STRING -> {
                // We want to get char[] instead of default String here
                int offset = jp.getTextOffset();
                int length = jp.getTextLength();
                char[] buffer = jp.getTextCharacters();

                charArraysToErase.add(buffer);

                char[] charArray = new char[length];
                System.arraycopy(buffer, offset, charArray, 0, length);
                yield charArray;
            }
            case VALUE_NUMBER_INT -> jp.getLongValue();
            case VALUE_NUMBER_FLOAT -> jp.getDoubleValue();
            case VALUE_TRUE, VALUE_FALSE -> jp.getBooleanValue();
            case VALUE_NULL -> null;
            default -> throw new IllegalStateException("Unexpected token: " + token);
        };
    }

    private Object[] parseArray(JsonParser jp, List<char[]> charArraysToErase) throws IOException {
        ArrayList<Object> result = new ArrayList<>();

        while (jp.nextToken() != JsonToken.END_ARRAY) {
            result.add(parseValue(jp, charArraysToErase));
        }

        return result.toArray();
    }

}
