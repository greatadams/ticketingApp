package com.ticketing.ticketing.events.controller;

import com.ticketing.ticketing.events.service.HoldService;
import com.ticketing.ticketing.events.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class SeatController {

    private final SeatService seatService;
    private final HoldService holdService;

    @PostMapping("/seats/{seatId}/sell")
    public ResponseEntity<Void>sellSeat(@PathVariable("seatId") UUID seatId,
                         @RequestParam String bookingId) {
       seatService.bookSeat(seatId,bookingId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/seats/{seatId}/hold")
    public  ResponseEntity<String> holdSeat(@PathVariable UUID seatId,
                                            @RequestParam String bookingId) {
        boolean got = holdService.placeHold(seatId,bookingId);
        return got
                ? ResponseEntity.ok("held")
                : ResponseEntity.status(HttpStatus.CONFLICT).body("Already held");
    }

}
