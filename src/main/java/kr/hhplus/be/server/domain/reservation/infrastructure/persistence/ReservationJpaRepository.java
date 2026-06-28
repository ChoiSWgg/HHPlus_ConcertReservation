package kr.hhplus.be.server.domain.reservation.infrastructure.persistence;

import kr.hhplus.be.server.domain.reservation.domain.model.Reservation;
import kr.hhplus.be.server.domain.reservation.domain.repository.ReservationRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public class ReservationJpaRepository implements ReservationRepository {
    private final SpringReservationJpa springReservationJpa;

    public ReservationJpaRepository(SpringReservationJpa springReservationJpa) {
        this.springReservationJpa = springReservationJpa;
    }

    @Override
    public Optional<Reservation> findByScheduleAndSeat(Long scheduleId, Long seatId) {
        return Optional.empty();
    }

    @Override
    public Reservation save(Reservation reservation) {
        return reservation;
    }
}
