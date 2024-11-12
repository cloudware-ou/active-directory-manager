package com.nortal.activedirectoryrestapi.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nortal.activedirectoryrestapi.entities.Commands;
import com.nortal.activedirectoryrestapi.services.CommandService;
import com.nortal.activedirectoryrestapi.services.CommandStatusChecker;
import com.nortal.activedirectoryrestapi.services.InputHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class RESTApiController {
    private final CommandService commandService;
    private final CommandStatusChecker commandStatusChecker;
    private final InputHandler inputHandler;


    private ResponseEntity<String> executeCommand(String command, String payload) {
        try{
            Long id = commandService.saveCommand(command, payload);
            Commands entity = commandStatusChecker.checkCommandStatus(id);
            if (entity.getExitCode() == 0){
                return ResponseEntity.ok(entity.getResult());
            } else {
                return ResponseEntity.badRequest().body(entity.getResult());
            }
        } catch (Exception e){
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @GetMapping("/users")
    public ResponseEntity<String> getUser(@RequestParam MultiValueMap<String, String> queryParams){
        try {
            String json = inputHandler.convertToJson(queryParams);
            return executeCommand("Get-ADUser", json);
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping(value="/users", consumes={"application/json"})
    public ResponseEntity<String> newUser(@RequestBody String payload) {
        try {
            inputHandler.validateJson(payload);
            return executeCommand("New-ADUser", payload);
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/users")
    public ResponseEntity<String> deleteUser(@RequestParam MultiValueMap<String, String> queryParams){
        try {
            String json = inputHandler.convertToJson(queryParams);
            return executeCommand("Remove-ADUser", json);
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
