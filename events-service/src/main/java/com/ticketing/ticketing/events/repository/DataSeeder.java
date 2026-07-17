package com.ticketing.ticketing.events.repository;

import com.ticketing.ticketing.events.model.Event;
import com.ticketing.ticketing.events.model.Seat;
import com.ticketing.ticketing.events.model.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;

    @Override
    public void run(String... args) {
        if (eventRepository.count() > 0) return;   // <-- the important guard

        Event event = new Event();
        event.setName("Spring Boot Live 2026");
        event.setCategory("CONCERT");              // category is nullable=false — must set it
        event.setDescription("Demo event for seat booking");
        eventRepository.save(event);

        List<Seat> seats = new ArrayList<>();
        for (char row = 'A'; row <= 'D'; row++) {
            for (int num = 1; num <= 5; num++) {
                Seat seat = new Seat();
                seat.setEventId(event.getId());
                seat.setSeatLabel("" + row + num);
                seats.add(seat);                   // status & version set themselves
            }
        }
        seatRepository.saveAll(seats);

        List<Seat> saved = seatRepository.findByEventId(event.getId());
        long available = saved.stream()
                .filter(s -> s.getStatus() == Status.AVAILABLE)
                .count();
        System.out.println(saved.size() + " seats, " + available + " AVAILABLE");
    }
}