package com.ticketing.booking.controller;

import com.ticketing.booking.model.Booking;
import com.ticketing.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/checkout")
    public ResponseEntity<Booking> checkout(@RequestParam String seatId,
                                            @RequestParam String bookingId) {
        Booking booking = bookingService.checkout(seatId, bookingId);
        return  ResponseEntity.ok(booking);

    }


}
//@PostMapping("/hold")
//public ResponseEntity<String> hold(@RequestParam String seatId,
//                                   @RequestParam String bookingId) {
//    boolean held = bookingService.holdSeat(seatId, bookingId);
//    return held
//            ? ResponseEntity.ok("held by gRPC")
//            : ResponseEntity.status(409).body("already held");
//}
//
//@PostMapping("/sell")
//public ResponseEntity<String> sell(@RequestParam String seatId,
//                                   @RequestParam String bookingId) {
//    BookingService.SellResult result = bookingService.sellSeat(seatId, bookingId);
//    return result.sold()
//            ? ResponseEntity.ok("sold by gRPC" + result.message())
//            : ResponseEntity.status(409).body("sell failed: "+ result.message());
//}