package com.nortal.activedirectoryrestapi.entities;

import com.fasterxml.jackson.databind.JsonNode;
import com.nortal.activedirectoryrestapi.converters.JsonAttributeConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnTransformer;

import java.time.OffsetDateTime;

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

    @Column(columnDefinition = "JSON")
    @Convert(converter = JsonAttributeConverter.class)
    @ColumnTransformer(write = "?::json")
    private JsonNode arguments;

    @NotNull
    private String commandStatus;

    @Column(columnDefinition = "JSON")
    @Convert(converter = JsonAttributeConverter.class)
    @ColumnTransformer(write = "?::json")
    private JsonNode result;

    private Integer exitCode;

    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime timestamp;
}
