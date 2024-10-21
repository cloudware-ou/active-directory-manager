package com.nortal.activedirectoryrestapi.controllers;

import com.nortal.activedirectoryrestapi.services.CommandService;
import com.nortal.activedirectoryrestapi.services.CommandStatusChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class RESTApiController {
    private final CommandService commandService;
    private final CommandStatusChecker commandStatusChecker;


    @GetMapping("/")
    public ResponseEntity<String> index() {
        return ResponseEntity.ok("Welcome to Active Directory Manager REST API!");
    }

    @PostMapping("/send-command")
    public ResponseEntity<String> executeCommand(@RequestBody Map<String, String> payload) {
        String command = payload.get("command");
        try{
            Long id = commandService.saveCommand(command);
            return ResponseEntity.ok(commandStatusChecker.checkCommandStatus(id));
        } catch (Exception e){
            return ResponseEntity.internalServerError().body(e.getMessage());
        }

    }

}
