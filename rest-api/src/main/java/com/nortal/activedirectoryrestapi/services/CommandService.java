package com.nortal.activedirectoryrestapi.services;

import com.nortal.activedirectoryrestapi.entities.Command;
import com.nortal.activedirectoryrestapi.repository.CommandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommandService {
    private final CommandRepository commandRepository;

    public void insertCommand(String command){
        Command commandEntity = new Command();
        commandEntity.setCommand(command);
        commandEntity.setCommandStatus("SENT");
        commandRepository.save(commandEntity);
    }
}
