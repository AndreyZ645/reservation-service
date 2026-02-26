package com.example.reservation_service.reservations;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reservation")
public class ReservationController {

    private static final Logger log = LoggerFactory.getLogger(ReservationController.class);

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService){
        this.reservationService = reservationService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getReservationById(
            @PathVariable("id") Long id
    ){
        log.info("Called getReservationById, id: {}", id);
        return ResponseEntity.ok().body(reservationService.getReservationById(id));
    }

    @GetMapping
    public List<Reservation> getAllReservations(
            @RequestParam(name = "roomId", required = false) Long roomId,
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "pageSize", required = false) Integer pageSize,
            @RequestParam(name = "pageNumber", required = false) Integer pageNumber
    ){
        log.info("Called getAllReservations");
        var filter = new ReservationSearchFilter(roomId, userId, pageSize, pageNumber);
        return reservationService.searchAllByFilter(filter);
    }

    @PostMapping
    public ResponseEntity<Reservation> createReservation(@RequestBody @Valid Reservation reservationToCreate){
        log.info("Called createReservation");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationService.createReservation(reservationToCreate));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Reservation> updateReservation(
            @PathVariable("id") Long id,
            @RequestBody @Valid Reservation reservationToUpdate
    ){
        log.info("Called updateReservationById id={}, reservationToUpdate={}", id, reservationToUpdate);
        return ResponseEntity.ok(reservationService.updateReservationById(id, reservationToUpdate));
    }

    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<Void> deleteReservation(
            @PathVariable("id") Long id
    ){
        log.info("Called deleteReservation id={}", id);
        reservationService.cancelReservation(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<Reservation> approveReservation(
            @PathVariable Long id
    ){
        log.info("called approveReservation id={}", id);
        return ResponseEntity.ok(reservationService.approveReservation(id));
    }
}
