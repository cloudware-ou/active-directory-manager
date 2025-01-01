package com.nortal.activedirectoryrestapi.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nortal.activedirectoryrestapi.exceptions.ADCommandExecutionException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ErrorHandler {

    public ResponseEntity<JsonNode> createErrorResponse(ADCommandExecutionException exception) {
        ObjectNode objectNode = (ObjectNode) exception.getError();
        int statusCode = exception.getStatusCode();
        objectNode.put("HttpStatusCode", statusCode);
        objectNode.put("Command", exception.getCommand());
        objectNode.put("Timestamp", exception.getTimestamp().toString());

        return ResponseEntity.status(statusCode).body(objectNode);
    }

    public ResponseEntity<JsonNode> createErrorResponse(JsonProcessingException exception) {
        ObjectMapper objectMapper = new ObjectMapper();
        int statusCode = 400;
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("HttpStatusCode", statusCode);
        objectNode.put("ErrorMessage", "Invalid JSON payload");

        return ResponseEntity.status(statusCode).body(objectNode);
    }
}
