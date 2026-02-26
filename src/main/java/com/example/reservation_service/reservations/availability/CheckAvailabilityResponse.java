package com.example.reservation_service.reservations.availability;

public record CheckAvailabilityResponse(
        String message,
        AvailabilityStatus status
) {

}
