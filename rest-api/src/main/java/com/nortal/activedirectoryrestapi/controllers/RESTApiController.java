package com.nortal.activedirectoryrestapi.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.nortal.activedirectoryrestapi.services.CommandWorker;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class RESTApiController {
    private final CommandWorker commandWorker;

    @Operation(summary = "Get users (Get-ADUser)")
    @GetMapping("/users")
    public ResponseEntity<JsonNode> getUser(@RequestParam MultiValueMap<String, Object> queryParams){
        return commandWorker.submitJob("Get-ADUser", queryParams);
    }

    @Operation(summary = "Create a new user (New-ADUser)")
    @PostMapping("/users")
    public ResponseEntity<JsonNode> newUser(@RequestBody Map<String, Object> payload) {
        return commandWorker.submitJob("New-ADUser", payload, HttpStatus.CREATED);
    }

    @Operation(summary = "Delete user (Remove-ADUser)")
    @DeleteMapping("/users")
    public ResponseEntity<JsonNode> removeUser(@RequestParam MultiValueMap<String, Object> queryParams){
        return commandWorker.submitJob("Remove-ADUser", queryParams);
    }

    @Operation(summary = "Update user details (Set-ADUser)")
    @PatchMapping("/users")
    public ResponseEntity<JsonNode> updateUser(@RequestBody Map<String, Object> payload) {
        return commandWorker.submitJob("Set-ADUser", payload);
    }

    @Operation(summary = "Get groups (Get-ADGroup)")
    @GetMapping("/groups")
    public ResponseEntity<JsonNode> getGroup(@RequestParam MultiValueMap<String, Object> queryParams){
        return commandWorker.submitJob("Get-ADGroup", queryParams);
    }

    @Operation(summary = "Create a new group (New-ADGroup)")
    @PostMapping("/groups")
    public ResponseEntity<JsonNode> newGroup(@RequestBody Map<String, Object> payload) {
        return commandWorker.submitJob("New-ADGroup", payload, HttpStatus.CREATED);
    }

    @Operation(summary = "Delete group (Remove-ADGroup)")
    @DeleteMapping("/groups")
    public ResponseEntity<JsonNode> removeGroup(@RequestParam MultiValueMap<String, Object> queryParams){
        return commandWorker.submitJob("Remove-ADGroup", queryParams);
    }

    @Operation(summary = "Update group details (Set-ADGroup)")
    @PatchMapping("/groups")
    public ResponseEntity<JsonNode> updateGroup(@RequestBody Map<String, Object> payload) {
        return commandWorker.submitJob("Set-ADGroup", payload);
    }

    @Operation(summary = "Get group members (Get-ADGroupMember)")
    @GetMapping ("/groups/members")
    public ResponseEntity<JsonNode> getGroupMember(@RequestParam MultiValueMap<String, Object> queryParams){
        return commandWorker.submitJob("Get-ADGroupMember", queryParams);
    }

    @Operation(summary = "Add group members (Add-ADGroupMember)")
    @PostMapping("/groups/members")
    public ResponseEntity<JsonNode> addGroupMember(@RequestBody Map<String, Object> payload) {
        return commandWorker.submitJob("Add-ADGroupMember", payload);
    }

    @Operation(summary = "Remove members from a group (Remove-ADGroupMember)")
    @DeleteMapping("/groups/members")
    public ResponseEntity<JsonNode> removeGroupMember(@RequestParam MultiValueMap<String, Object> queryParams){
        return commandWorker.submitJob("Remove-ADGroupMember", queryParams);
    }

    @Operation(summary = "Enable Active Directory account (Enable-ADAccount)")
    @PatchMapping("/accounts/enable")
    public ResponseEntity<JsonNode> enableAccount(@RequestBody Map<String, Object> payload){
        return commandWorker.submitJob("Enable-ADAccount", payload);
    }

    @Operation(summary = "Disable Active Directory account (Disable-ADAccount)")
    @PatchMapping("/accounts/disable")
    public ResponseEntity<JsonNode> disableAccount(@RequestBody Map<String, Object> payload){
        return commandWorker.submitJob("Disable-ADAccount", payload);
    }

    @Operation(summary = "Change/Reset Active Directory account password (Set-ADAccountPassword)")
    @PatchMapping("/accounts/password")
    public ResponseEntity<JsonNode> setAccountPassword(@RequestBody Map<String, Object> payload){
        return commandWorker.submitJob("Set-ADAccountPassword", payload);
    }
}
