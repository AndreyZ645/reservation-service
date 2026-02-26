package com.example.reservation_service.reservations.availability;

import com.example.reservation_service.reservations.ReservationRepository;
import com.example.reservation_service.reservations.ReservationService;
import com.example.reservation_service.reservations.ReservationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReservationAvailabilityService {

    private static final Logger log = LoggerFactory.getLogger(ReservationAvailabilityService.class);

    private final ReservationRepository repository;

    public ReservationAvailabilityService(ReservationRepository reservationRepository) {
        this.repository = reservationRepository;
    }

    public boolean isReservationAvailable(
            Long roomId,
            LocalDate startDate,
            LocalDate endDate
    ){
        if(!endDate.isAfter(startDate)) {
            throw new IllegalArgumentException("End date should be at least 1 day earlier than start date");
        }
        List<Long> conflictIds = repository.findConflictReservationIds(
                roomId,
                startDate,
                endDate,
                ReservationStatus.APPROVED);
        log.info("Confict with ids {}", conflictIds);
        return conflictIds.isEmpty();
    }
}
