package kr.hhplus.be.server.domain.reservation.infrastructure.persistence;

import kr.hhplus.be.server.domain.reservation.domain.model.Reservation;
import kr.hhplus.be.server.domain.reservation.domain.repository.ReservationRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ReservationRepositoryImpl implements ReservationRepository {

    private final ReservationJpaRepository reservationJpaRepository;

    public ReservationRepositoryImpl(ReservationJpaRepository reservationJpaRepository) {
        this.reservationJpaRepository = reservationJpaRepository;
    }

    @Override
    public Optional<Reservation> findByScheduleAndSeat(Long scheduleId, Long seatId) {
        return reservationJpaRepository.findByScheduleIdAndSeatId(scheduleId, seatId)
            .map(ReservationEntity::toDomain);
    }

    @Override
    public Reservation save(Reservation reservation) {
        ReservationEntity entity = ReservationEntity.from(reservation);
        return reservationJpaRepository.save(entity).toDomain();
    }

    @Override
    public Optional<Reservation> findById(Long reservationId) {
        return reservationJpaRepository.findById(reservationId)
            .map(ReservationEntity::toDomain);
    }

    @Override
    public List<Reservation> findAllByScheduleId(Long scheduleId) {
        return reservationJpaRepository.findAllByScheduleId(scheduleId)
            .stream()
            .map(ReservationEntity::toDomain)
            .toList();
    }
}
