package com.nortal.activedirectoryrestapi.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TerminatingError extends RuntimeException {
    public TerminatingError(String message, Throwable cause) {
        super(message);
        Logger logger = LoggerFactory.getLogger(TerminatingError.class);
        logger.error(message, cause);
    }
}
