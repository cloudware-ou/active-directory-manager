package com.nortal.activedirectoryrestapi.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nortal.activedirectoryrestapi.exceptions.ADCommandExecutionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ErrorHandler {
    private final Map<String, HttpStatus> errorCodes = new HashMap<>(
                 Map.ofEntries(
                         Map.entry("Directory object not found.", HttpStatus.NOT_FOUND),
                         Map.entry("The specified account already exists.", HttpStatus.CONFLICT),
                         Map.entry("The specified group already exists.", HttpStatus.CONFLICT),
                         Map.entry("Cannot find an object with identity:  under: .", HttpStatus.NOT_FOUND)
                 )
         );

    public ResponseEntity<JsonNode> createErrorResponse(ADCommandExecutionException exception) {
        ObjectNode objectNode = (ObjectNode) exception.getError();
        String key = objectNode.get("ErrorMessage").asText().replaceAll("'[^']*'", "");
        HttpStatus httpStatus = this.errorCodes.getOrDefault(key, HttpStatus.BAD_REQUEST);

        objectNode.put("HttpStatusCode", httpStatus.value());
        objectNode.put("Command", exception.getCommand());
        objectNode.put("Timestamp", exception.getTimestamp().toString());

        return ResponseEntity.status(httpStatus).body(objectNode);
    }
}
