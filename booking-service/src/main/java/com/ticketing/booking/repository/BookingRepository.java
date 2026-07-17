package com.ticketing.booking.repository;

import com.ticketing.booking.model.Booking;
import com.ticketing.booking.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {
    List<Booking> findByBookingStatusAndCreatedAtBefore(BookingStatus status, Instant cutoff);
}
