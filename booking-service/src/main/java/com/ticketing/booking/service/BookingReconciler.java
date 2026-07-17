package com.ticketing.booking.service;

import com.ticketing.booking.model.Booking;
import com.ticketing.booking.model.BookingStatus;
import com.ticketing.booking.repository.BookingRepository;
import com.ticketing.contracts.seat.SeatReservationGrpc;
import com.ticketing.contracts.seat.SeatStatusRequest;
import com.ticketing.contracts.seat.SeatStatusResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingReconciler {
    @GrpcClient("events")
    private SeatReservationGrpc.SeatReservationBlockingStub eventStub;

    private final BookingRepository bookingRepository;
    private final BookingRecorder recorder;

    private static final Duration STALE_AFTER = Duration.ofSeconds(30);

    @Scheduled(fixedDelay = 15_000)
    public void reconcile() {
        Instant cutoff = Instant.now().minus(STALE_AFTER);
        List<Booking> stranded = bookingRepository
                .findByBookingStatusAndCreatedAtBefore(BookingStatus.PENDING, cutoff);

        if (stranded.isEmpty()) return;
        log.info("Reconciling {} stranded booking(s)", stranded.size());

        for (Booking booking : stranded) {
            try {
                SeatStatusResponse status = eventStub.getSeatStatus(
                        SeatStatusRequest.newBuilder()
                                .setSeatId(booking.getSeatId())
                                .build());

                // Not just "is the seat sold?" — sold and sold to this booking.
                boolean weWon = status.getSold()
                        && booking.getBookingId().equals(status.getBookingId());

                recorder.resolve(booking, weWon ? BookingStatus.CONFIRMED : BookingStatus.FAILED);
                log.info("Resolved booking={} seat={} → {}",
                        booking.getBookingId(), booking.getSeatId(),
                        weWon ? "CONFIRMED" : "FAILED");

            } catch (Exception e) {
                log.warn("Could not reconcile booking={} — will retry next sweep: {}",
                        booking.getBookingId(), e.getMessage());
            }
        }
    }
}
