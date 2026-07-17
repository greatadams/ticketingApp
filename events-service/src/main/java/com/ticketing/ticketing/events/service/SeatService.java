package com.ticketing.ticketing.events.service;

import com.ticketing.ticketing.events.exception.HoldRequiredException;
import com.ticketing.ticketing.events.exception.SeatAlreadySoldException;
import com.ticketing.ticketing.events.model.Status;
import com.ticketing.ticketing.events.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class
SeatService {

    private final SeatRepository seatRepository;
    private final HoldService holdService;

//    @Transactional
//    public Seat bookSeat(UUID seatId,String bookingId) {
//        //load/retrieve the seat--from db
//        Seat seat = seatRepository.findById(seatId).orElseThrow(() -> new RuntimeException("Seat not available"));
//
////        //verify the seat belong to that event
////        if(!seat.getEventId().equals(eventId)) {
////            throw new RuntimeException("Seat does not belong to this event");
////        }
//
//        //check availability--of that seat
//       if (seat.getStatus()==Status.SOLD){
//           throw new SeatAlreadySoldException("Seat is already sold");
//       }
//
//       //mark as sold--if available sell
//        seat.setStatus(Status.SOLD);
//        seat.setBookingId(bookingId);
//
//        //save the ticket just sold to db
//       return seatRepository.save(seat);
//
//    };
//
//}

    @Transactional
    public void bookSeat(UUID seatId, String bookingId) {
    //.you may only buy seat you currently hold
        if(!holdService.holdsSeat(seatId, bookingId)){
            throw new HoldRequiredException("No valid hold for this seat");
        }

        //2.atomic sell
     int rowsUpdated = seatRepository.sellIfAvailable(seatId,bookingId,Status.SOLD,Status.AVAILABLE);

     if(rowsUpdated==0){
         //our WHERE matched nothing: either no such seat or it's already SOLD
         throw new SeatAlreadySoldException("Seat is already sold");
     }
     //rowUpdated == 1 -> we won the row it's ours
        holdService.releaseHold(seatId,bookingId);
    }
}