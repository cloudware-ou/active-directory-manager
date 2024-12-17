package com.nortal.activedirectoryrestapi.repository;

import com.nortal.activedirectoryrestapi.entities.Command;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommandRepository extends JpaRepository<Command, Long> {
// not used but let it remain here for now
}
