package com.ticketing.ticketing.events.service;

import com.ticketing.ticketing.events.model.Seat;
import com.ticketing.ticketing.events.model.Status;
import com.ticketing.ticketing.events.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HoldService {

    //redis-cli in java
    private final StringRedisTemplate stringRedisTemplate;

    private static final Duration HOLD_TTL = Duration.ofMinutes(10);
    private final SeatRepository seatRepository;

    public boolean placeHold(UUID seatId, String bookingId){
        // ask the source of truth first: is this seat even available?
        Seat seat = seatRepository.findById(seatId).orElse(null);
        if (seat == null || seat.getStatus() != Status.AVAILABLE){
            return false; //already sold(doesn't exist)
        }
        String key = "hold:seat:" + seatId;
        Boolean acquired = stringRedisTemplate.opsForValue()
                //read the name: "set if absent"
                //SET hold:seat:A1 alice NX EX 600
                //if this seat is already held by another buyer return false and the buyer bounces
                .setIfAbsent(key, bookingId,HOLD_TTL);
        return Boolean.TRUE.equals(acquired);
    }

    //confirming the right buyer is holding the seat
    public  boolean holdsSeat(UUID seatId, String bookingId){
        String key = "hold:seat:" + seatId;
        String holder = stringRedisTemplate.opsForValue().get(key); //who hold it(or null if no hold)
        return bookingId.equals(holder);//is the holder me

    }

    private static final RedisScript<Long> RELEASE_IF_OWNER = RedisScript.of(
            "if redis.call('GET', KEYS[1]) == ARGV[1] then return redis.call('DEL', KEYS[1]) else return 0 end",
            Long.class
    );
    //runs after seat is sold deletes the hold because the hold job is finished
    public void releaseHold(UUID seatId, String bookingId){
//        String key = "hold:seat:" + seatId;
//        stringRedisTemplate.delete(key);

        stringRedisTemplate.execute(RELEASE_IF_OWNER,
                List.of("hold:seat:" + seatId),   // KEYS[1]
                bookingId);
    }

}
