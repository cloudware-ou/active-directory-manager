package com.nortal.activedirectoryrestapi.misc;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ErrorObject {
    private final String command;
    private final String errorMessage;
    private final int httpStatusCode;
    private final String timestamp;
}
