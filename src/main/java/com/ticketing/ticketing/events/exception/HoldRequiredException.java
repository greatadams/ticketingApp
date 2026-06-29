package com.ticketing.ticketing.events.exception;

public class HoldRequiredException extends RuntimeException {
  public HoldRequiredException(String message) {
    super(message);
  }
}
