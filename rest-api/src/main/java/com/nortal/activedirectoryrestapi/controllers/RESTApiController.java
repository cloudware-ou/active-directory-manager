package com.nortal.activedirectoryrestapi.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.nortal.activedirectoryrestapi.Constants;
import com.nortal.activedirectoryrestapi.services.CommandWorker;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class RESTApiController {
    private final CommandWorker commandWorker;

    @Operation(summary = "Get users (Get-ADUser)")
    @GetMapping("/users")
    public ResponseEntity<String> getUser(@RequestParam MultiValueMap<String, Object> queryParams){
        return commandWorker.submitJob(Constants.GET_USER, queryParams);
    }

    @Operation(summary = "Create a new user (New-ADUser)")
    @PostMapping("/users")
    public ResponseEntity<String> newUser(@RequestBody JsonNode payload) {
        return commandWorker.submitJob(Constants.NEW_USER, payload, HttpStatus.CREATED);
    }

    @Operation(summary = "Delete user (Remove-ADUser)")
    @DeleteMapping("/users")
    public ResponseEntity<String> removeUser(@RequestParam MultiValueMap<String, Object> queryParams){
        return commandWorker.submitJob(Constants.REMOVE_USER, queryParams);
    }

    @Operation(summary = "Update user details (Set-ADUser)")
    @PutMapping("/users")
    public ResponseEntity<String> updateUser(@RequestBody JsonNode payload) {
        return commandWorker.submitJob(Constants.SET_USER, payload);
    }

    @Operation(summary = "Get groups (Get-ADGroup)")
    @GetMapping("/groups")
    public ResponseEntity<String> getGroup(@RequestParam MultiValueMap<String, Object> queryParams){
        return commandWorker.submitJob(Constants.GET_GROUP, queryParams);
    }

    @Operation(summary = "Create a new group (New-ADGroup)")
    @PostMapping("/groups")
    public ResponseEntity<String> newGroup(@RequestBody JsonNode payload) {
        return commandWorker.submitJob(Constants.NEW_GROUP, payload, HttpStatus.CREATED);
    }

    @Operation(summary = "Delete group (Remove-ADGroup)")
    @DeleteMapping("/groups")
    public ResponseEntity<String> removeGroup(@RequestParam MultiValueMap<String, Object> queryParams){
        return commandWorker.submitJob(Constants.REMOVE_GROUP, queryParams);
    }

    @Operation(summary = "Update group details (Set-ADGroup)")
    @PutMapping("/groups")
    public ResponseEntity<String> updateGroup(@RequestBody JsonNode payload) {
        return commandWorker.submitJob(Constants.SET_GROUP, payload);
    }

    @Operation(summary = "Get group members (Get-ADGroupMember)")
    @GetMapping ("/groups/members")
    public ResponseEntity<String> getGroupMember(@RequestParam MultiValueMap<String, Object> queryParams){
        return commandWorker.submitJob(Constants.GET_GROUP_MEMBER, queryParams);
    }

    @Operation(summary = "Add group members (Add-ADGroupMember)")
    @PostMapping("/groups/members")
    public ResponseEntity<String> addGroupMember(@RequestBody JsonNode payload) {
        return commandWorker.submitJob(Constants.ADD_GROUP_MEMBER, payload);
    }

    @Operation(summary = "Remove members from a group (Remove-ADGroupMember)")
    @DeleteMapping("/groups/members")
    public ResponseEntity<String> removeGroupMember(@RequestParam MultiValueMap<String, Object> queryParams){
        return commandWorker.submitJob(Constants.REMOVE_GROUP_MEMBER, queryParams);
    }

    @Operation(summary = "Enable Active Directory account (Enable-ADAccount)")
    @PutMapping("/accounts/enable")
    public ResponseEntity<String> enableAccount(@RequestBody JsonNode payload){
        return commandWorker.submitJob(Constants.ENABLE_ACCOUNT, payload);
    }

    @Operation(summary = "Disable Active Directory account (Disable-ADAccount)")
    @PutMapping("/accounts/disable")
    public ResponseEntity<String> disableAccount(@RequestBody JsonNode payload){
        return commandWorker.submitJob(Constants.DISABLE_ACCOUNT, payload);
    }

    @Operation(summary = "Change/Reset Active Directory account password (Set-ADAccountPassword)")
    @PutMapping("/accounts/password")
    public ResponseEntity<String> setAccountPassword(@RequestBody JsonNode payload){
        return commandWorker.submitJob(Constants.SET_ACCOUNT_PASSWORD, payload);
    }
}
