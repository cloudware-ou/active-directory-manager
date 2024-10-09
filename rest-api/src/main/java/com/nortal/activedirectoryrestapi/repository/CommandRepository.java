package com.nortal.activedirectoryrestapi.repository;

import com.nortal.activedirectoryrestapi.entities.Command;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface CommandRepository extends JpaRepository<Command, Long> {
    Optional<Command> findByCommandStatus(String commandStatus);
}
