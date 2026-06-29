package com.ticketing.ticketing.events.exception;

public class SeatAlreadySoldException extends RuntimeException {
  public SeatAlreadySoldException(String message) {
    super(message);
  }
}
