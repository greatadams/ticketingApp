package com.ticketing.booking.repository;

import com.ticketing.booking.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutboxRepository extends JpaRepository<OutboxEvent, UUID> {
    List<OutboxEvent> findBySentFalseOrderByCreatedAtAsc();
}
