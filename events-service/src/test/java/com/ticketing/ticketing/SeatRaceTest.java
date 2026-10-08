package com.ticketing.ticketing;


import com.ticketing.ticketing.events.model.Seat;
import com.ticketing.ticketing.events.repository.SeatRepository;
import com.ticketing.ticketing.events.service.SeatService;
import com.ticketing.ticketing.events.service.HoldService;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Test;


import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SeatRaceTest {

    @Autowired
    SeatService seatService;
    @Autowired
    SeatRepository seatRepository;
    @Autowired
    HoldService holdService;

    @Test
    void twenty_buyers_one_seat() throws InterruptedException {
        // grab one available seat to fight over
        Seat seat = new Seat();
        seat.setEventId(UUID.randomUUID());
        seat.setSeatLabel("RACE-" + UUID.randomUUID());

        seat = seatRepository.saveAndFlush(seat);
        UUID seatId = seat.getId();

        int threads = 20;
        var pool = Executors.newFixedThreadPool(threads);
        var startLine = new CountDownLatch(1);     // hold all threads at the gate
        var successes = new AtomicInteger();
        var failures  = new AtomicInteger();

        for (int i = 0; i < threads; i++) {
            final String buyer = "buyer-" + i;
            pool.submit(() -> {
                try {
                    startLine.await();             // everyone waits here...
                    if (holdService.placeHold(seatId, buyer)) {
                        seatService.bookSeat(seatId, buyer);
                        successes.incrementAndGet();
                    } else {
                        failures.incrementAndGet();
                    }
                } catch (Exception e) {
                    failures.incrementAndGet();     // SeatAlreadySoldException lands here
                }
                return null;
            });
        }

        startLine.countDown();                      // ...then GO, all at once
        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);

        System.out.println("successes = " + successes.get());
        System.out.println("failures  = " + failures.get());

        // the seat can only be sold ONCE — anything else is a bug
        assertThat(successes.get()).isEqualTo(1);
    }
}