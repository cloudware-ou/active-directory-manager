package com.nortal.activedirectoryrestapi.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.nortal.activedirectoryrestapi.entities.Command;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommandService {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public Long saveCommand(String command, JsonNode args) {
        Command commandEntity = new Command();
        commandEntity.setCommand(command);
        commandEntity.setArguments(args);
        commandEntity.setCommandStatus("PENDING");
        entityManager.persist(commandEntity);
        entityManager.flush();  // Ensure the entity is saved immediately
        return commandEntity.getId();
    }

    @Transactional(readOnly = true)
    public Command getCommand(Long id) {
        Command entity = entityManager.find(Command.class, id);
        entityManager.refresh(entity);  // Refresh to get latest data
        return entity;
    }

}

