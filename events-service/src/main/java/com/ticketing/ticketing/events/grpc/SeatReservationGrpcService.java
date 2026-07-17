package com.ticketing.ticketing.events.grpc;

import com.ticketing.contracts.seat.*;
import com.ticketing.ticketing.events.exception.HoldRequiredException;
import com.ticketing.ticketing.events.exception.SeatAlreadySoldException;
import com.ticketing.ticketing.events.model.Seat;
import com.ticketing.ticketing.events.model.Status;
import com.ticketing.ticketing.events.repository.SeatRepository;
import com.ticketing.ticketing.events.service.HoldService;
import com.ticketing.ticketing.events.service.SeatService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.UUID;


@GrpcService
@RequiredArgsConstructor
public class SeatReservationGrpcService extends SeatReservationGrpc.SeatReservationImplBase {
    private final HoldService holdService;
    private final SeatService seatService;
    private final SeatRepository seatRepository;

    @Override
    public void hold(HoldRequest request, StreamObserver<HoldResponse> responseObserver){
        boolean held = holdService.placeHold(
                UUID.fromString(request.getSeatId()),
                request.getBookingId()
        );
        HoldResponse response = HoldResponse.newBuilder()
                .setHeld(held)
                .build();

        responseObserver.onNext(response); //send the response
        responseObserver.onCompleted(); //signal "done"
    }

    @Override
    public void sell (SellRequest request, StreamObserver<SellResponse> responseObserver){
        SellResponse.Builder reply = SellResponse.newBuilder();
        try {
            seatService.bookSeat(
                    UUID.fromString(request.getSeatId()),
                    request.getBookingId());
            reply.setSold(true).setMessage("SOLD");
        }catch (HoldRequiredException | SeatAlreadySoldException e){
            reply.setSold(false).setMessage(e.getMessage());
        }
        responseObserver.onNext(reply.build());
        responseObserver.onCompleted();
    }
    @Override
    public void getSeatStatus(SeatStatusRequest request,
                              StreamObserver<SeatStatusResponse> responseObserver) {

        Seat seat = seatRepository.findById(UUID.fromString(request.getSeatId()))
                .orElse(null);

        SeatStatusResponse.Builder reply = SeatStatusResponse.newBuilder();
        if (seat != null && seat.getStatus() == Status.SOLD) {
            reply.setSold(true)
                    .setBookingId(seat.getBookingId() == null ? "" : seat.getBookingId());
        }
        // seat unsold or missing → sold=false, booking_id="" (proto3 defaults)

        responseObserver.onNext(reply.build());
        responseObserver.onCompleted();
    }
}
