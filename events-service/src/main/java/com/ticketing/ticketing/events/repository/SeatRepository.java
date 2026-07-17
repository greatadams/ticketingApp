package com.ticketing.ticketing.events.repository;

import com.ticketing.ticketing.events.model.Seat;
import com.ticketing.ticketing.events.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface SeatRepository extends JpaRepository<Seat, UUID> {
    List<Seat> findByEventId(UUID eventId);

    @Modifying
    @Query("UPDATE Seat s SET s.status = :sold, " +
    "s.bookingId = :bookingId " +
    "WHERE s.id = :seatId AND s.status = :available"
    )
    int sellIfAvailable(@Param("seatId") UUID seatId,
                        @Param("bookingId") String bookingId,
                        @Param("sold") Status sold,
                        @Param("available") Status available
    );

;

}
