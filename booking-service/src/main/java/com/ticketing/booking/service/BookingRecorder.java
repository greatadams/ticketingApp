package com.ticketing.booking.service;

import com.ticketing.booking.model.Booking;
import com.ticketing.booking.model.BookingStatus;
import com.ticketing.booking.model.OutboxEvent;
import com.ticketing.booking.repository.BookingRepository;
import com.ticketing.booking.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class BookingRecorder {

    private final BookingRepository bookingRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;


    //(attempt started)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Booking createPending(String seatId, String bookingId) {
        Booking booking = new Booking();
        booking.setSeatId(seatId);
        booking.setBookingId(bookingId);
        return bookingRepository.save(booking);   // commits when THIS method returns
    }

    //(outcome known )
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Booking resolve(Booking booking, BookingStatus status) {
        booking.setBookingStatus(status);
        booking.setUpdatedAt(Instant.now());
        return bookingRepository.save(booking);
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Booking resolveAndEnqueue(Booking booking, BookingStatus status, Object event,
                                     String eventId, String topic, String key) {
        booking.setBookingStatus(status);
        booking.setUpdatedAt(Instant.now());
        booking = bookingRepository.save(booking);

        if (status == BookingStatus.CONFIRMED) {
            OutboxEvent row = new OutboxEvent();
            row.setEventId(eventId);
            row.setTopic(topic);
            row.setMessageKey(key);
            try {
                row.setPayload(objectMapper.writeValueAsString(event));
            } catch (Exception e) {
                throw new IllegalStateException("Cannot serialize event", e);
            }
            outboxRepository.save(row);
        }

        return booking;// ← ONE transaction. Booking + outbox commit together, or neither does.
        //bookingRepository.save(booking);   // bookings table
        //outboxRepository.save(row);        // outbox table
        //they commited together or row both back
    }

}
