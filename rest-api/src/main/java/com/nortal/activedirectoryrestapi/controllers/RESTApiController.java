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
        return commandWorker.submitJob("Get-ADUser", queryParams);
    }

    @PostMapping(value="/users", consumes={"application/json"})
    public ResponseEntity<String> newUser(@RequestBody String payload) {
        return commandWorker.submitJob("New-ADUser", payload);
    }

    @DeleteMapping("/users")
    public ResponseEntity<String> deleteUser(@RequestParam MultiValueMap<String, Object> queryParams){
        queryParams.addAll("Confirm", List.of(false));
        return commandWorker.submitJob("Remove-ADUser", queryParams);
    }
}
