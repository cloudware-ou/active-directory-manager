package com.nortal.activedirectoryrestapi.controllers;

import com.nortal.activedirectoryrestapi.services.CommandWorker;
import com.nortal.activedirectoryrestapi.services.ErrorHandler;
import com.nortal.activedirectoryrestapi.services.JSONHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RESTApiController {
    private final CommandWorker commandWorker;


    @GetMapping("/users")
    public ResponseEntity<String> getUser(@RequestParam MultiValueMap<String, Object> queryParams){
        return commandWorker.submitJob("Get-ADUser", queryParams);
    }

    @PostMapping("/users")
    public ResponseEntity<String> newUser(@RequestBody String payload) {
        return commandWorker.submitJob("New-ADUser", payload);
    }

    @DeleteMapping("/users")
    public ResponseEntity<String> removeUser(@RequestParam MultiValueMap<String, Object> queryParams){
        return commandWorker.submitJob("Remove-ADUser", queryParams);
    }

    @PatchMapping("/users")
    public ResponseEntity<String> updateUser(@RequestBody String payload) {
        return commandWorker.submitJob("Set-ADUser", payload);
    }

    @GetMapping("/groups")
    public ResponseEntity<String> getGroup(@RequestParam MultiValueMap<String, Object> queryParams){
        return commandWorker.submitJob("Get-ADGroup", queryParams);
    }

    @PostMapping("/groups")
    public ResponseEntity<String> newGroup(@RequestBody String payload) {
        return commandWorker.submitJob("New-ADGroup", payload);
    }

    @DeleteMapping("/groups")
    public ResponseEntity<String> removeGroup(@RequestParam MultiValueMap<String, Object> queryParams){
        return commandWorker.submitJob("Remove-ADGroup", queryParams);
    }

    @PatchMapping("/groups")
    public ResponseEntity<String> updateGroup(@RequestBody String payload) {
        return commandWorker.submitJob("Set-ADGroup", payload);
    }

    @GetMapping ("/groups/members")
    public ResponseEntity<String> getGroupMember(@RequestParam MultiValueMap<String, Object> queryParams){
        return commandWorker.submitJob("Get-ADGroupMember", queryParams);
    }

    @PostMapping("/groups/members")
    public ResponseEntity<String> addGroupMember(@RequestBody String payload) {
        return commandWorker.submitJob("Add-ADGroupMember", payload);
    }

    @DeleteMapping("/groups/members")
    public ResponseEntity<String> removeGroupMember(@RequestParam MultiValueMap<String, Object> queryParams){
        return commandWorker.submitJob("Remove-ADGroupMember", queryParams);
    }

    @PatchMapping("/accounts/enable")
    public ResponseEntity<String> enableAccount(@RequestBody String payload){
        return commandWorker.submitJob("Enable-ADAccount", payload);
    }
    @PatchMapping("/accounts/disable")
    public ResponseEntity<String> disableAccount(@RequestBody String payload){
        return commandWorker.submitJob("Disable-ADAccount", payload);
    }

    @PatchMapping("/accounts/password")
    public ResponseEntity<String> setAccountPassword(@RequestBody String payload){
        return commandWorker.submitJob("Set-ADAccountPassword", payload);
    }
}
