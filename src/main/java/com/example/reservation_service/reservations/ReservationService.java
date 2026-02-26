package com.example.reservation_service.reservations;

import com.example.reservation_service.reservations.availability.ReservationAvailabilityService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReservationService {

    private final ReservationRepository repository;
    private final ReservationMapper mapper;
    private final ReservationAvailabilityService availabilityService;

    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);

    public ReservationService(ReservationRepository repository, ReservationMapper mapper, ReservationAvailabilityService availabilityService) {
        this.repository = repository;
        this.mapper = mapper;
        this.availabilityService = availabilityService;
    }

    public Reservation getReservationById(Long id) {
        ReservationEntity reservationEntity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found reservation by id: " + id));
        return mapper.toDomain(reservationEntity);
    }

    public List<Reservation> searchAllByFilter(
            ReservationSearchFilter filter
    ){
        int pageSize = filter.pageSize() != null ? filter.pageSize() : 10;
        int pageNumber = filter.pageNumber() != null ? filter.pageNumber() : 0;
        var pageable = Pageable.ofSize(pageSize).withPage(pageNumber);

        List<ReservationEntity> allEntities = repository.searchAllByFilter(filter.roomId(), filter.userId(), pageable);
        return allEntities.stream().map(mapper::toDomain)
                .toList();
    }

    public Reservation createReservation(Reservation reservationToCreate) {
        if(reservationToCreate.status() != null){
            throw new IllegalArgumentException("Status should be empty");
        }
        if(!reservationToCreate.endDate().isAfter(reservationToCreate.startDate())) {
            throw new IllegalArgumentException("End date should be at least 1 day earlier than start date");
        }
        var entityToSave = mapper.toEntity(reservationToCreate);
        entityToSave.setStatus(ReservationStatus.PENDING);
        repository.save(entityToSave);
        return mapper.toDomain(entityToSave);
    }

    public Reservation updateReservationById(Long id, Reservation reservationToUpdate){
        var reservationEntity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found reservation by id: " + id));
        if(reservationEntity.getStatus() != ReservationStatus.PENDING){
            throw new IllegalStateException("Cannot modify reservation with status " + reservationEntity.getStatus());
        }
        if(!reservationToUpdate.endDate().isAfter(reservationToUpdate.startDate())) {
            throw new IllegalArgumentException("End date should be at least 1 day earlier than start date");
        }
        var reservationToSave = mapper.toEntity(reservationToUpdate);
        reservationToSave.setId(reservationEntity.getId());
        reservationToSave.setStatus(ReservationStatus.PENDING);
        repository.save(reservationToSave);
        return mapper.toDomain(reservationToSave);
    }

    @Transactional
    public void cancelReservation(Long id) {
        var reservationEntity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found reservation by id: " + id));
        if(reservationEntity.getStatus() != ReservationStatus.PENDING){
            throw new IllegalStateException("Cannot cancel reservation with status " + reservationEntity.getStatus());
        }
        repository.setStatus(id, ReservationStatus.CANCELLED);
        log.info("Successfully cancelled reservation with id {}", id);
    }

    public Reservation approveReservation(Long id){
        ReservationEntity reservation = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found element by id " + id));
        if(reservation.getStatus() != ReservationStatus.PENDING){
            throw new IllegalStateException("Cannot approve reservation with status " + reservation.getStatus());
        }
        if(!availabilityService.isReservationAvailable(reservation.getRoomId(), reservation.getStartDate(), reservation.getEndDate())){
            throw new IllegalStateException("Cannot approve reservation with status " + reservation.getStatus());
        }
        reservation.setStatus(ReservationStatus.APPROVED);
        repository.save(reservation);
        return mapper.toDomain(reservation);
    }
}
