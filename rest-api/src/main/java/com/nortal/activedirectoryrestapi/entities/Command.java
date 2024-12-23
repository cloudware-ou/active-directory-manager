package com.nortal.activedirectoryrestapi.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name="commands")
public class Command {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime timestamp;
}
