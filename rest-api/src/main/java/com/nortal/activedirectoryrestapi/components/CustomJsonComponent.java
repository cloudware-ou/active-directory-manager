package com.nortal.activedirectoryrestapi.components;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import org.springframework.boot.jackson.JsonComponent;

@JsonComponent
public class CustomJsonComponent {

    public static class Deserializer extends JsonDeserializer<Map<String, Object>> {

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
            Object[] array = new Object[10]; // Temporary size
            int index = 0;

            while (jp.nextToken() != JsonToken.END_ARRAY) {
                if (index >= array.length) {
                    // Resize array dynamically
                    Object[] newArray = new Object[array.length * 2];
                    System.arraycopy(array, 0, newArray, 0, array.length);
                    array = newArray;
                }
                array[index++] = parseValue(jp);
            }

            // Trim array to actual size
            Object[] resultArray = new Object[index];
            System.arraycopy(array, 0, resultArray, 0, index);
            return resultArray;
        }

    }

}
