package com.ticketing.booking.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name="outbox")
public class OutboxEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String eventId;        // the same eventId that goes in the event

    @Column(nullable = false)
    private String topic;

    @Column(nullable = false)
    private String messageKey;     // the Kafka key (bookingId)

    @Column(nullable = false, columnDefinition = "text")
    private String payload;        // the event, as JSON

    @Column(nullable = false)
    private boolean sent = false;  // born unsent

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    private Instant sentAt;

}
