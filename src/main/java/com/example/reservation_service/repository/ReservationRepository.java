package com.example.reservation_service.repository;

import com.example.reservation_service.model.ReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {
}
