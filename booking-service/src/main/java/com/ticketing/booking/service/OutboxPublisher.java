package com.ticketing.booking.service;

import com.ticketing.booking.model.OutboxEvent;
import com.ticketing.booking.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {
    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Scheduled(fixedDelay = 3_000)
    @Transactional
    public void flush() {
        List<OutboxEvent> pending = outboxRepository.findBySentFalseOrderByCreatedAtAsc();
        if (pending.isEmpty()) return;

        for (OutboxEvent row : pending) {
            try {
                kafkaTemplate.send(row.getTopic(), row.getMessageKey(), row.getPayload())
                        .get();                       // wait for the broker's ack
                row.setSent(true);
                row.setSentAt(Instant.now());
                outboxRepository.save(row);
                log.info("Outbox → sent eventId={}", row.getEventId());
            } catch (Exception e) {
                log.warn("Outbox → send failed for eventId={}, will retry: {}",
                        row.getEventId(), e.getMessage());
            }
        }
    }
}
