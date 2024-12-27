package com.nortal.activedirectoryrestapi.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.nortal.activedirectoryrestapi.entities.Command;
import com.nortal.activedirectoryrestapi.exceptions.ADCommandExecutionException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

@Service
@RequiredArgsConstructor
public class CommandWorker {
    private final CommandService commandService;
    private final JSONHandler jsonHandler;
    private final ErrorHandler errorHandler;
    private final NotificationListener notificationListener;

    public Command waitForResult(Long id) {
        return notificationListener.getCompletedCommand(id);
    }

    public Command executeCommand(String command, JsonNode payload) throws ADCommandExecutionException {
        Long id = commandService.saveCommand(command, payload);
        Command entity = this.waitForResult(id);
        if (entity.getExitCode() == 0){
            return entity;
        } else {
            throw new ADCommandExecutionException(entity.getCommand(), entity.getResult(), entity.getExitCode(), entity.getTimestamp());
        }
    }

    public ResponseEntity<JsonNode> submitJob(String command, JsonNode payload, HttpStatusCode httpStatusCode) {
        try{
            jsonHandler.secureJson(payload);
            return ResponseEntity.status(httpStatusCode).body(this.executeCommand(command, payload).getResult());
        } catch (ADCommandExecutionException e) {
            return errorHandler.createErrorResponse(e);
        }
    }

    public ResponseEntity<JsonNode> submitJob(String command, JsonNode payload){
        return submitJob(command, payload, HttpStatus.OK);
    }

    public ResponseEntity<JsonNode> submitJob(String command, MultiValueMap<String, Object> params){
        JsonNode json = jsonHandler.convertToJson(params);
        return submitJob(command, json, HttpStatus.OK);
    }





}
