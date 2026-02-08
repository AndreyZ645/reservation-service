package com.example.reservation_service.service;

import com.example.reservation_service.model.Reservation;
import com.example.reservation_service.model.ReservationEntity;
import com.example.reservation_service.model.ReservationStatus;
import com.example.reservation_service.repository.ReservationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ReservationService {

    private final ReservationRepository repository;

    public ReservationService(ReservationRepository repository){
        this.repository = repository;
    }

    public Reservation getReservationById(Long id) {
        ReservationEntity reservationEntity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found reservation by id: " + id));
        return toDomainReservation(reservationEntity);
    }

    public List<Reservation> findAllReservations(){
        List<ReservationEntity> allEntities = repository.findAll();
        return allEntities.stream().map(this::toDomainReservation)
                .toList();
    }

    public Reservation createReservation(Reservation reservationToCreate) {
        if(reservationToCreate.id() != null){
            throw new IllegalArgumentException("Id should be empty");
        }
        if(reservationToCreate.status() != null){
            throw new IllegalArgumentException("Status should be empty");
        }
        var entityToSave = new ReservationEntity(
                null,
                reservationToCreate.userId(),
                reservationToCreate.roomId(),
                reservationToCreate.startDate(),
                reservationToCreate.endDate(),
                ReservationStatus.PENDING
        );
        var savedEntity = repository.save(entityToSave);
        return toDomainReservation(savedEntity);
    }

    public Reservation updateReservationById(Long id, Reservation reservationToUpdate){
        var reservationEntity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found reservation by id: " + id));
        if(reservationEntity.getStatus() != ReservationStatus.PENDING){
            throw new IllegalStateException("Cannot modify reservation: status = " + reservationEntity.getStatus());
        }
        reservationEntity.setUserId(reservationToUpdate.userId());
        reservationEntity.setRoomId(reservationToUpdate.roomId());
        reservationEntity.setStartDate(reservationToUpdate.startDate());
        reservationEntity.setEndDate(reservationToUpdate.endDate());
        repository.save(reservationEntity);
        return toDomainReservation(reservationEntity);
    }

    public void deleteReservation(Long id){
        if(!repository.existsById(id)) {
            throw new EntityNotFoundException("Not found element by id " + id);
        }
        repository.deleteById(id);
    }

    public Reservation approveReservation(Long id){
        ReservationEntity reservation = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found element by id " + id));
        if(reservation.getStatus() != ReservationStatus.PENDING){
            throw new IllegalStateException("Cannot approve reservation: status = " + reservation.getStatus());
        }
        if(isReservationConflict(reservation)){
            throw new IllegalStateException("Cannot approve reservation: status = " + reservation.getStatus());
        }
        reservation.setStatus(ReservationStatus.APPROVED);
        repository.save(reservation);
        return toDomainReservation(reservation);
    }

    private boolean isReservationConflict(ReservationEntity reservation){
        return repository.findAll().stream()
                .filter(existingReservation ->
                        !reservation.getId().equals(existingReservation.getId()) &&
                        reservation.getRoomId().equals(existingReservation.getRoomId()) &&
                        existingReservation.getStatus().equals(ReservationStatus.APPROVED) &&
                        reservation.getStartDate().isBefore(existingReservation.getEndDate()) &&
                        existingReservation.getStartDate().isBefore(reservation.getEndDate()))
                .anyMatch(existingReservation -> true);
    }

    private Reservation toDomainReservation(ReservationEntity reservationEntity) {
        return new Reservation(
                reservationEntity.getId(),
                reservationEntity.getUserId(),
                reservationEntity.getRoomId(),
                reservationEntity.getStartDate(),
                reservationEntity.getEndDate(),
                reservationEntity.getStatus()
        );
    }
}
