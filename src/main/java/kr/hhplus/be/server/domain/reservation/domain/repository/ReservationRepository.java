package kr.hhplus.be.server.domain.reservation.domain.repository;

import kr.hhplus.be.server.domain.reservation.domain.model.Reservation;
import java.util.Optional;

public interface ReservationRepository {
    Optional<Reservation> findByScheduleAndSeat(Long scheduleId, Long seatId);
    Reservation save(Reservation reservation);
}
