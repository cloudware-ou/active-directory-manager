package com.nortal.activedirectoryrestapi.controllers;

import com.nortal.activedirectoryrestapi.entities.Commands;
import com.nortal.activedirectoryrestapi.services.CommandService;
import com.nortal.activedirectoryrestapi.services.CommandStatusChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class RESTApiController {
    private final CommandService commandService;
    private final CommandStatusChecker commandStatusChecker;

    private ResponseEntity<String> executeCommand(String command) {
        try{
            Long id = commandService.saveCommand(command);
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

    @GetMapping("/")
    public ResponseEntity<String> index() {
        return ResponseEntity.ok("Welcome to Active Directory Manager REST API!");
    }

    @PostMapping("/send-command")
    public ResponseEntity<String> executeCommand(@RequestBody Map<String, String> payload) {
        String command = payload.get("command");
        return executeCommand(command);
    }

    @GetMapping("/get-user")
    public ResponseEntity<String> getUser(@RequestParam List<String> params){
        String command = "Get-ADUser " + String.join(" ", params);
        return executeCommand(command);
    }

    @PostMapping("/new-user")
    public ResponseEntity<String> newUser(@RequestBody Map<String, String> payload) {
        String command = "New-ADUser " + payload.get("user");
        return executeCommand(command);
    }

}
