package com.nortal.activedirectoryrestapi.services;

import com.nortal.activedirectoryrestapi.entities.Commands;
import com.nortal.activedirectoryrestapi.repository.CommandRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CommandService {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public Long saveCommand(String command) {
        Commands commandsEntity = new Commands();
        commandsEntity.setCommand(command);
        commandsEntity.setCommandStatus("PENDING");
        entityManager.persist(commandsEntity);
        entityManager.flush();  // Ensure the entity is saved immediately
        return commandsEntity.getId();
    }

    @Transactional(readOnly = true)
    public Commands getCommand(Long id) {
        Commands entity = entityManager.find(Commands.class, id);
        entityManager.refresh(entity);  // Refresh to get latest data
        return entity;
    }



}

