package kr.hhplus.be.server.domain.reservation.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringReservationJpa extends JpaRepository<ReservationEntity, Long> {
}
