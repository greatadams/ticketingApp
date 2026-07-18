# Ticketing API
The Ticketing API is an API with 3 micro services

## Services
- **booking-service**- orchestrates checkout and owns bookings.
- **event-service**- owns seat inventory. The only thing allowed to mark a seat SOLD.
- **notification-service** — reacts to confirmed bookings. Sends the email.

## Architecture
```
BOOKING-SERVICE —— gRPC —— EVENT-SERVICE
        |
      KAFKA
        |
NOTIFICATION-SERVICE
```

Each communicates amongst themselves.
Booking-service communicates with event-service via gRPC, and booking-service communicates with notification-service via Kafka.

## The problem: overselling
I created a race test at first with twenty concurrent threads hitting the same seat. Ten different buyers got the same seat . until I implemented a conditional update to fix this and prevent overselling—adding a query to the table that writes the set status to sold and stamps the booking ID and any seat ID for which the status is available to be sold

## Seat holds
Also implemented a Redis using the TTL_HOLD so a seat being processed to purchase is placed on hold for a certain hold time, and TTL is placed to expire at a certain minute if that purchase is abandoned .

## Failure handling: crash recovery
A recovery mechanism, a bookingRecorder and a bookingReconciler for a crash that happens mid purchase —a bookingRecorder creates a pending status as an attempt it made on a seat, and bookingReconciler asks over gRPC who owns the seat, then calls the resolve method in the bookingRecorder to record   CONFIRMED if someone won  or if the attempt was FAILED.


## Why gRPC here, Kafka there
Because when you need two services to remain in constant communication to prevent untraceable or Ghost purchase, we need one service to wait for the other -we use the method and message in the PROTO file where booking -services calls to event-service and event-service answers - and the booking-service waits for a response before proceeding to anything

And we use kafka for notification because you ask the question what important? A user will be ok with not getting an email about there booking immediately, that's why we use Kafka. The Kafka producer is placed in the booking services after a purchase is done, the booking service registers it to the Kafka producer topic, and the notification-service as a Kafka listener and goes to listen to it.

## Failure handling: event delivery
And in case Kafka is ever down, we came up with an outbox recovery mechanism that stops events from being lost if kafka is down -because the confirmation event goes to the outbox table in the same transaction as the booking. So the poller reads the unsent row every set seconds and ships them,and keeps retrying until Kafka is back up.

In order to prevent double emailing to the user, we introduce the idempotency concept- now the notification has to have a table(processed_events) which checks if an email has already been sent, and it doesn’t send it again.



## Out of scope.
Gateway and auth are out of scope — this project focuses on the concurrency and consistency core.
