package com.nortal.activedirectoryrestapi.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Commands {
    @Id
    @GeneratedValue
    private Long id;

    @NotNull
    private String command;

    @Column(columnDefinition = "TEXT")
    private String arguments;

    @NotNull
    private String commandStatus;

    @Column(columnDefinition = "TEXT")
    private String result;

    private Integer exitCode;
}
