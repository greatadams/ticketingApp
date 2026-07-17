package com.ticketing.booking.event;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookingEventPublisher {

    public static final String TOPIC = "booking.events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishConfirmed(BookingConfirmedEvent bookingConfirmedEvent) {
        kafkaTemplate.send(TOPIC, bookingConfirmedEvent.bookingId(), bookingConfirmedEvent);
    }


}
