package com.nortal.activedirectoryrestapi.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nortal.activedirectoryrestapi.exceptions.ADCommandExecutionException;
import com.nortal.activedirectoryrestapi.services.CommandWorker;
import com.nortal.activedirectoryrestapi.services.ErrorHandler;
import com.nortal.activedirectoryrestapi.services.JSONHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RESTApiController {
    private final CommandWorker commandWorker;
    private final JSONHandler jsonHandler;
    private final ErrorHandler errorHandler;


    @GetMapping("/users")
    public ResponseEntity<String> getUser(@RequestParam MultiValueMap<String, Object> queryParams){
        try {
            String json = jsonHandler.convertToJson(queryParams);
            return ResponseEntity.ok().body(commandWorker.executeCommand("Get-ADUser", json).getResult());
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ADCommandExecutionException e) {
            return errorHandler.createErrorResponse(e);
        } catch (InterruptedException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping(value="/users", consumes={"application/json"})
    public ResponseEntity<String> newUser(@RequestBody String payload) {
        try {
            jsonHandler.validateJson(payload);
            return ResponseEntity.ok().body(commandWorker.executeCommand("New-ADUser", payload).getResult());
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ADCommandExecutionException e) {
            return errorHandler.createErrorResponse(e);
        } catch (InterruptedException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @DeleteMapping("/users")
    public ResponseEntity<String> deleteUser(@RequestParam MultiValueMap<String, Object> queryParams){
        queryParams.addAll("Confirm", List.of(false));
        try {
            String json = jsonHandler.convertToJson(queryParams);
            return ResponseEntity.ok().body(commandWorker.executeCommand("Remove-ADUser", json).getResult());
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ADCommandExecutionException e) {
            return errorHandler.createErrorResponse(e);
        } catch (InterruptedException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
