package com.nortal.activedirectoryrestapi.controllers;

import com.nortal.activedirectoryrestapi.services.CommandWorker;
import com.nortal.activedirectoryrestapi.services.ErrorHandler;
import com.nortal.activedirectoryrestapi.services.JSONHandler;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RESTApiController {
    private final CommandWorker commandWorker;

    @Operation(summary = "Get users (Get-ADUser)")
    @GetMapping("/users")
    public ResponseEntity<String> getUser(@RequestParam MultiValueMap<String, Object> queryParams){
        return commandWorker.submitJob("Get-ADUser", queryParams);
    }

    @Operation(summary = "Create a new user (New-ADUser)")
    @PostMapping("/users")
    public ResponseEntity<String> newUser(@RequestBody String payload) {
        return commandWorker.submitJob("New-ADUser", payload);
    }

    @Operation(summary = "Delete user (Remove-ADUser)")
    @DeleteMapping("/users")
    public ResponseEntity<String> removeUser(@RequestParam MultiValueMap<String, Object> queryParams){
        return commandWorker.submitJob("Remove-ADUser", queryParams);
    }

    @Operation(summary = "Update user details (Set-ADUser)")
    @PutMapping("/users")
    public ResponseEntity<String> updateUser(@RequestBody String payload) {
        return commandWorker.submitJob("Set-ADUser", payload);
    }

    @Operation(summary = "Get groups (Get-ADGroup)")
    @GetMapping("/groups")
    public ResponseEntity<String> getGroup(@RequestParam MultiValueMap<String, Object> queryParams){
        return commandWorker.submitJob("Get-ADGroup", queryParams);
    }

    @Operation(summary = "Create a new group (New-ADGroup)")
    @PostMapping("/groups")
    public ResponseEntity<String> newGroup(@RequestBody String payload) {
        return commandWorker.submitJob("New-ADGroup", payload);
    }

    @Operation(summary = "Delete group (Remove-ADGroup)")
    @DeleteMapping("/groups")
    public ResponseEntity<String> removeGroup(@RequestParam MultiValueMap<String, Object> queryParams){
        return commandWorker.submitJob("Remove-ADGroup", queryParams);
    }

    @Operation(summary = "Update group details (Set-ADGroup)")
    @PutMapping("/groups")
    public ResponseEntity<String> updateGroup(@RequestBody String payload) {
        return commandWorker.submitJob("Set-ADGroup", payload);
    }

    @Operation(summary = "Get group members (Get-ADGroupMember)")
    @GetMapping ("/groups/members")
    public ResponseEntity<String> getGroupMember(@RequestParam MultiValueMap<String, Object> queryParams){
        return commandWorker.submitJob("Get-ADGroupMember", queryParams);
    }

    @Operation(summary = "Add group members (Add-ADGroupMember)")
    @PostMapping("/groups/members")
    public ResponseEntity<String> addGroupMember(@RequestBody String payload) {
        return commandWorker.submitJob("Add-ADGroupMember", payload);
    }

    @Operation(summary = "Remove members from a group (Remove-ADGroupMember)")
    @DeleteMapping("/groups/members")
    public ResponseEntity<String> removeGroupMember(@RequestParam MultiValueMap<String, Object> queryParams){
        return commandWorker.submitJob("Remove-ADGroupMember", queryParams);
    }

    @Operation(summary = "Enable Active Directory account (Enable-ADAccount)")
    @PutMapping("/accounts/enable")
    public ResponseEntity<String> enableAccount(@RequestBody String payload){
        return commandWorker.submitJob("Enable-ADAccount", payload);
    }

    @Operation(summary = "Disable Active Directory account (Disable-ADAccount)")
    @PutMapping("/accounts/disable")
    public ResponseEntity<String> disableAccount(@RequestBody String payload){
        return commandWorker.submitJob("Disable-ADAccount", payload);
    }

    @Operation(summary = "Change/Reset Active Directory account password (Set-ADAccountPassword)")
    @PutMapping("/accounts/password")
    public ResponseEntity<String> setAccountPassword(@RequestBody String payload){
        return commandWorker.submitJob("Set-ADAccountPassword", payload);
    }
}
