package com.ticketing.notification;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "processed_events")
public class ProcessedEvent {
    @Id
    private String eventId; //the eventId is the primary key

    @Column(nullable = false,updatable = false)
    private Instant processedAt = Instant.now();

    public ProcessedEvent(String eventId) {
        this.eventId = eventId;
    }

}
