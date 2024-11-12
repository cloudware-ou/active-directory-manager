package com.nortal.activedirectoryrestapi.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nortal.activedirectoryrestapi.entities.Commands;
import com.nortal.activedirectoryrestapi.exceptions.ADCommandExecutionException;
import com.nortal.activedirectoryrestapi.misc.ErrorObject;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import static java.lang.Thread.sleep;

@Service
@RequiredArgsConstructor
public class CommandWorker {

    private final CommandService commandService;

    public Commands checkCommandStatus(Long id) throws InterruptedException {
        while (true) {
            Commands refreshedEntity = commandService.getCommand(id);  // Refresh from DB

            if (refreshedEntity.getCommandStatus().equals("COMPLETED")) {
                return refreshedEntity;
            }

            sleep(1000);  // Polling interval
        }
    }

    public Commands executeCommand(String command, String payload) throws ADCommandExecutionException, InterruptedException {
            Long id = commandService.saveCommand(command, payload);
            Commands entity = this.checkCommandStatus(id);
            if (entity.getExitCode() == 0){
                return entity;
            } else {
                throw new ADCommandExecutionException(entity.getResult(), entity.getTimestamp());
            }
    }


}
