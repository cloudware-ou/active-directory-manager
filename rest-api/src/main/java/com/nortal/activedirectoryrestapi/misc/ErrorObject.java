package com.nortal.activedirectoryrestapi.misc;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ErrorObject {
    private final String command;
    private final JsonNode errorMessage;
    private final int httpStatusCode;
    private final String timestamp;
}
