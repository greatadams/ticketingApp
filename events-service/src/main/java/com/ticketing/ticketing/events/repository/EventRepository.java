package com.ticketing.ticketing.events.repository;

import com.ticketing.ticketing.events.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {
}
