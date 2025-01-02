package com.nortal.activedirectoryrestapi.components;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import org.springframework.boot.jackson.JsonComponent;

@JsonComponent
public class CustomJsonDeserializer extends JsonDeserializer<Map<String, Object>>{

    @Override
    public Map<String, Object> deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
        Map<String, Object> result = parseObject(jsonParser);
        jsonParser.close();
        return result;
    }

    private Map<String, Object> parseObject(JsonParser jp) throws IOException {
        Map<String, Object> map = new HashMap<>();
        JsonToken token;

        while ((token = jp.nextToken()) != JsonToken.END_OBJECT) {
            if (token == JsonToken.FIELD_NAME) {
                String fieldName = jp.currentName();
                jp.nextToken(); // Move to the value
                map.put(fieldName, parseValue(jp));
            }
        }
        return map;
    }

    private Object parseValue(JsonParser jp) throws IOException {
        JsonToken token = jp.currentToken();

        return switch (token) {
            case START_OBJECT -> parseObject(jp);
            case START_ARRAY -> parseArray(jp);
            case VALUE_STRING -> {
                // We want to get char[] instead of default String here
                int offset = jp.getTextOffset();
                int length = jp.getTextLength();
                char[] charArray = new char[length];
                System.arraycopy(jp.getTextCharacters(), offset, charArray, 0, length);
                yield charArray;
            }
            case VALUE_NUMBER_INT -> jp.getLongValue();
            case VALUE_NUMBER_FLOAT -> jp.getDoubleValue();
            case VALUE_TRUE, VALUE_FALSE -> jp.getBooleanValue();
            case VALUE_NULL -> null;
            default -> throw new IllegalStateException("Unexpected token: " + token);
        };
    }

    private Object[] parseArray(JsonParser jp) throws IOException {
        ArrayList<Object> result = new ArrayList<>();

        while (jp.nextToken() != JsonToken.END_ARRAY) {
            result.add(parseValue(jp));
        }

        return result.toArray();
    }

}
