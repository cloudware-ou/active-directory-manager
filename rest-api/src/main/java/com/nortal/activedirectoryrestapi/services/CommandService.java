package com.nortal.activedirectoryrestapi.services;

import com.nortal.activedirectoryrestapi.entities.Commands;
import com.nortal.activedirectoryrestapi.repository.CommandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommandService {
    private final CommandRepository commandRepository;

    public void insertCommand(String command){
        Commands commandsEntity = new Commands();
        commandsEntity.setCommand(command);
        commandsEntity.setCommandStatus("PENDING");
        commandRepository.save(commandsEntity);
    }
}
