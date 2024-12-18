package com.nortal.activedirectoryrestapi.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nortal.activedirectoryrestapi.Constants;
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

    public ResponseEntity<String> submitJobMeta(String command, String validJson){
        try {
            HttpStatusCode httpStatusCode = HttpStatus.OK;
            if (List.of(Constants.NEW_USER, Constants.NEW_GROUP).contains(command)){
                httpStatusCode = HttpStatus.CREATED;
            }
            return ResponseEntity.status(httpStatusCode).body(this.executeCommand(command, validJson).getResult());
        } catch (ADCommandExecutionException e) {
            return errorHandler.createErrorResponse(e);
        } catch (InterruptedException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    public ResponseEntity<String> submitJob(String command, JsonNode payload){
            return submitJobMeta(command, payload.toPrettyString());
    }

    public ResponseEntity<String> submitJob(String command, MultiValueMap<String, Object> params){
        try {
            String json = jsonHandler.convertToJson(params);
            return submitJobMeta(command, json);
        }catch (JsonProcessingException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }





}
