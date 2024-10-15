package com.nortal.activedirectoryrestapi.repository;

import com.nortal.activedirectoryrestapi.entities.Commands;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface CommandRepository extends JpaRepository<Commands, Long> {
    Optional<Commands> findByCommandStatus(String commandStatus);
}
