package com.ticketing.booking.model;

public enum BookingStatus {
    PENDING, //created, checkout started, not yet resolved
    CONFIRMED, //seat successfully sold
    FAILED //hold expired,seat gone, or sell rejected
}
