package com.nortal.activedirectoryrestapi.exceptions;

import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
public class ADCommandExecutionException extends Exception {
    private final String command;
    private final OffsetDateTime timestamp;
    public ADCommandExecutionException(String command, String message, OffsetDateTime timestamp) {
        super(message);
        this.command = command;
        this.timestamp = timestamp;
    }
}
