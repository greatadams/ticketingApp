package com.ticketing.booking.service;

import com.ticketing.booking.event.BookingConfirmedEvent;
import com.ticketing.booking.event.BookingEventPublisher;
import com.ticketing.booking.model.Booking;
import com.ticketing.booking.model.BookingStatus;
import com.ticketing.booking.repository.BookingRepository;
import com.ticketing.contracts.seat.*;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {

    @GrpcClient("events")
    private SeatReservationGrpc.SeatReservationBlockingStub eventStub;

    private final BookingRepository bookingRepository;
    private final BookingEventPublisher publisher;
    private final BookingRecorder bookingRecorder;

    public Booking checkout(String seatId, String bookingId){

       Booking booking = bookingRecorder.createPending(seatId, bookingId);//row committed right here

        //2.hold the seat over gRPC
        HoldRequest holdRequest = HoldRequest.newBuilder()
                .setSeatId(seatId)
                .setBookingId(bookingId)
                .build();
        boolean held = eventStub.hold(holdRequest).getHeld();

        if (!held) {
            return bookingRecorder.resolve(booking,BookingStatus.FAILED);
        }

        //payment would happen here

        //4.sell the seat over gRPC
        SellRequest sellRequest =SellRequest.newBuilder()
                .setSeatId(seatId)
                .setBookingId(bookingId)
                .build();
        SellResponse sellResponse = eventStub.sell(sellRequest);

        if (sellResponse.getSold()) {
            String eventId = UUID.randomUUID().toString();
            BookingConfirmedEvent event = new BookingConfirmedEvent(
                    eventId, bookingId, seatId, Instant.now());
            booking = bookingRecorder.resolveAndEnqueue(booking, BookingStatus.CONFIRMED,
                    event, eventId, BookingEventPublisher.TOPIC, bookingId);
        } else {
            booking = bookingRecorder.resolve(booking, BookingStatus.FAILED);
        }

//        booking = bookingRecorder.resolve(booking,
//                sellResponse.getSold() ? BookingStatus.CONFIRMED : BookingStatus.FAILED);


//        if (booking.getBookingStatus().equals(BookingStatus.CONFIRMED)) {
//            publisher.publishConfirmed(new BookingConfirmedEvent(
//                    UUID.randomUUID().toString(), //fresh eventid
//                    bookingId,
//                    seatId,
//                    Instant.now()
//            ));
//        }
    return  booking;

    }



}

//    public  boolean holdSeat(String seatId,String bookingId){
//        //1.build the protobuf request(the wrap-> java value -> gRPC message)
//        HoldRequest request = HoldRequest.newBuilder()
//                .setSeatId(seatId)
//                .setBookingId(bookingId)
//                .build();
//        //2. call across the wire -looks local, secretly dials 9090
//        HoldResponse holdResponse = eventStub.hold(request);
//
//        //3.rad the result(unwrap-gRPC message -> java value)
//        return holdResponse.getHeld();
//    }

//    public SellResult sellSeat(String seatId,String bookingId){
//        SellRequest request = SellRequest.newBuilder()
//                .setSeatId(seatId)
//                .setBookingId(bookingId)
//                .build();
//        SellResponse sellResponse = eventStub.sell(request);
//        return new SellResult(sellResponse.getSold(), sellResponse.getMessage());
//    }
//    // a small record to carry both fields
//public record SellResult(boolean sold, String message) {}

