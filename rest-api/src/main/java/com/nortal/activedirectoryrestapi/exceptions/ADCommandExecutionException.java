package com.nortal.activedirectoryrestapi.exceptions;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.OffsetDateTime;

@Getter
@RequiredArgsConstructor
public class ADCommandExecutionException extends Exception {
    private final String command;
    private final JsonNode error;
    private final int statusCode;
    private final OffsetDateTime timestamp;
}
