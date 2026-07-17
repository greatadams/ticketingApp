package com.ticketing.booking.event;

import java.time.Instant;

public record BookingConfirmedEvent(
        String eventId,     //unique per event - consumers depends on this/
        String bookingId,
        String seatId,
        Instant occurredAt
) {
}
