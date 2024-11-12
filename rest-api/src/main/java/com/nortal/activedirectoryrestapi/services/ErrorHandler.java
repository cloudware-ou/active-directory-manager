package com.nortal.activedirectoryrestapi.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nortal.activedirectoryrestapi.exceptions.ADCommandExecutionException;
import com.nortal.activedirectoryrestapi.misc.ErrorObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ErrorHandler {
    private final Map<String, HttpStatus> errorCodes = new HashMap<>(
                 Map.ofEntries(
                         Map.entry("Directory object not found", HttpStatus.NOT_FOUND),
                         Map.entry("The specified account already exists", HttpStatus.CONFLICT)
                 )
         );

    public ResponseEntity<String> createErrorResponse(ADCommandExecutionException exception) {
        HttpStatus httpStatus = this.errorCodes.getOrDefault(exception.getMessage(), HttpStatus.BAD_REQUEST);
        ErrorObject errorObject = new ErrorObject(exception.getMessage(), httpStatus.value(), exception.getTimestamp().toString());
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return ResponseEntity.status(httpStatus).body(objectMapper.writeValueAsString(errorObject));
        } catch (JsonProcessingException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
