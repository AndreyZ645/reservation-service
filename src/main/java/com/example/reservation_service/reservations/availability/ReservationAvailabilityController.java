package com.example.reservation_service.reservations.availability;

import com.example.reservation_service.reservations.ReservationController;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reservation/availability")
public class ReservationAvailabilityController {

    private static final Logger log = LoggerFactory.getLogger(ReservationAvailabilityController.class);

    private final ReservationAvailabilityService service;

    public ReservationAvailabilityController(ReservationAvailabilityService service) {
        this.service = service;
    }

    @PostMapping("/check")
    public ResponseEntity<CheckAvailabilityResponse> checkAvailability(
            @Valid CheckAvailabilityRequest request
    ) {
        log.info("Called method CheckAvailability: request = {}", request);
        var isAvailable = service.isReservationAvailable(
                request.roomId(),
                request.startDate(),
                request.endDate()
        );
        var message = isAvailable ?
                "Room " + request.roomId() + "is available for reservation" :
                "Room " + request.roomId() + " is not available for reservation";
        var status = isAvailable ? AvailabilityStatus.AVAILABLE : AvailabilityStatus.RESERVED;
        return ResponseEntity.ok().body(new CheckAvailabilityResponse(message, status));
    }
}
