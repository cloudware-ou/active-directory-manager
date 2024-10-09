package com.nortal.activedirectoryrestapi.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Command {
    @Id
    @GeneratedValue
    private Long id;
    @NotNull
    private String command;
    @NotNull
    private String commandStatus;
    private String result;
}
