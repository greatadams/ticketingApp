package com.ticketing.notification;

import com.ticketing.notification.repository.ProcessedEventRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingConfirmedListener {

    private final ProcessedEventRepository processedEventRepository;

    @KafkaListener(topics = "booking.events", groupId = "notification-service")
    @Transactional
    public void onBookingConfirmed(BookingConfirmedEvent bookingConfirmedEvent) {
        //already handle skip
        if (processedEventRepository.existsById(bookingConfirmedEvent.eventId())) {
            log.info("Skipping duplicate eventId={}", bookingConfirmedEvent.eventId());
            return;
        }

        try {
            //claim it before sending-record first-act second
            processedEventRepository.save(new ProcessedEvent(bookingConfirmedEvent.eventId()));   // claim it
            //if it already been claimed-postgres reject it
        } catch (DataIntegrityViolationException e) {
            log.info("Race — another thread claimed eventId={}", bookingConfirmedEvent.eventId());
            return;
        }

        //only reached if we won the claim
        log.info("📧 Sending confirmation email — booking={} seat={} (eventId={})",
                bookingConfirmedEvent.bookingId(), bookingConfirmedEvent.seatId(), bookingConfirmedEvent.eventId());
    }

}

