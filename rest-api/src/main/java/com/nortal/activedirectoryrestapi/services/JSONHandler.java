package com.nortal.activedirectoryrestapi.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nortal.activedirectoryrestapi.exceptions.ADCommandExecutionException;
import com.nortal.activedirectoryrestapi.misc.ErrorObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.util.Map;
import java.util.stream.Collectors;

@Service
public class JSONHandler {
    public String convertToJson(MultiValueMap<String, String> multiValueMap) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(
                multiValueMap.entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> entry.getValue().size() == 1 ? entry.getValue().getFirst() : entry.getValue()
                        )));
    }

    public void validateJson(String json) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.readTree(json);
    }

    public ResponseEntity<String> createErrorResponseJson(HttpStatus status, ADCommandExecutionException exception) {
        ErrorObject errorObject = new ErrorObject(exception.getMessage(), status.value(), exception.getTimestamp().toString());
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return ResponseEntity.status(status).body(objectMapper.writeValueAsString(errorObject));
        } catch (JsonProcessingException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
