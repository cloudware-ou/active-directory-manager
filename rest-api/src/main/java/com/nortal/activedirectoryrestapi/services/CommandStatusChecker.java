package com.nortal.activedirectoryrestapi.services;

import com.nortal.activedirectoryrestapi.entities.Commands;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommandStatusChecker {

    private final CommandService commandService;

    public Commands checkCommandStatus(Long id) throws Exception {
        while (true) {
            Commands refreshedEntity = commandService.getCommand(id);  // Refresh from DB

            if (refreshedEntity.getCommandStatus().equals("COMPLETED")) {
                return refreshedEntity;
            }

            Thread.sleep(1000);  // Polling interval
        }
    }
}
