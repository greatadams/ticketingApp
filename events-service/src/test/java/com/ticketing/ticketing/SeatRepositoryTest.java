package com.ticketing.ticketing;

import com.ticketing.ticketing.events.model.Seat;
import com.ticketing.ticketing.events.model.Status;
import com.ticketing.ticketing.events.repository.SeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;


@SpringBootTest
@Transactional
class SeatRepositoryTest {

    @Autowired
    SeatRepository seatRepository;

    @Test
    void sellIfAvailable_sellsAnAvailableSeat() {
        //Arrange - a fresh AVAILABLE seat
        Seat seat = new Seat();
        seat.setEventId(UUID.randomUUID());
        seat.setSeatLabel("Z9");
        seat = seatRepository.save(seat);

        //Act -sell it
        int rows = seatRepository.sellIfAvailable(
                seat.getId(), "alice", Status.SOLD, Status.AVAILABLE);

        //Assert - exactly one row change, and it's now SOLD/alice
        assertThat(rows).isEqualTo(1);
        Seat reloaded = seatRepository.findById(seat.getId()).orElse(null);
        assertThat(reloaded.getStatus()).isEqualTo(Status.SOLD);
        assertThat(reloaded.getBookingId()).isEqualTo("alice");

    }

    @Test
    void  sellIfAvailable_refusesAnAlreadySoldSeat() {
        //Arrange- a seat that's already SOLD
        Seat seat = new Seat();
        seat.setEventId(UUID.randomUUID());
        seat.setSeatLabel("Z8");
        seat.setStatus(Status.SOLD);
        seat.setBookingId("bob");
        seat = seatRepository.save(seat);

        //Act-try to sell it to someone else
        int rows = seatRepository.sellIfAvailable(
                seat.getId(), "alice", Status.SOLD, Status.AVAILABLE
        );

        //Assert--zero row changed,still bob
        assertThat(rows).isEqualTo(0);
        Seat reloaded = seatRepository.findById(seat.getId()).orElse(null);
        assertThat(reloaded.getBookingId()).isEqualTo("bob");
    }



}
