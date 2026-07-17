package com.ticketing.ticketing.events.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class HoldRequiredException extends RuntimeException {
    public HoldRequiredException(String message) {
        super(message);
    }
}
