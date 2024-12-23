package com.nortal.activedirectoryrestapi.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nortal.activedirectoryrestapi.entities.Command;
import com.nortal.activedirectoryrestapi.exceptions.ADCommandExecutionException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommandWorker {

    private final CommandService commandService;
    private final JSONHandler jsonHandler;
    private final ErrorHandler errorHandler;
    private final NotificationListener notificationListener;


    public Command waitForResult(Long id) throws InterruptedException {
        return notificationListener.getCompletedCommand(id);
    }

    public Command executeCommand(String command, String payload) throws ADCommandExecutionException, InterruptedException {
            Long id = commandService.saveCommand(command, payload);
            Command entity = this.waitForResult(id);
            if (entity.getExitCode() == 0){
                return entity;
            } else {
                throw new ADCommandExecutionException(entity.getCommand(), entity.getResult(), entity.getTimestamp());
            }
    }

    public ResponseEntity<String> submitJob(String command, JsonNode payload, HttpStatusCode httpStatusCode) {
        try{
            jsonHandler.secureJson(payload);
            return ResponseEntity.status(httpStatusCode).body(this.executeCommand(command, payload.toPrettyString()).getResult());
        } catch (ADCommandExecutionException e) {
            return errorHandler.createErrorResponse(e);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    public ResponseEntity<String> submitJob(String command, JsonNode payload){
        return submitJob(command, payload, HttpStatus.OK);
    }

    public ResponseEntity<String> submitJob(String command, MultiValueMap<String, Object> params){
        try {
            JsonNode json = jsonHandler.convertToJson(params);
            return submitJob(command, json, HttpStatus.OK);
        }catch (JsonProcessingException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }





}
