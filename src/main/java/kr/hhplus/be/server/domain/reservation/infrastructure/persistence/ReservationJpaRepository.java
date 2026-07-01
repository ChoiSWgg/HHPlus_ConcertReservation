package kr.hhplus.be.server.domain.reservation.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReservationJpaRepository extends JpaRepository<ReservationEntity, Long> {

    // 특정 스케줄 + 좌석 조합으로 조회
    Optional<ReservationEntity> findByScheduleIdAndSeatId(Long scheduleId, Long seatId);

    // 특정 스케줄의 전체 예약 목록 조회
    List<ReservationEntity> findAllByScheduleId(Long scheduleId);
}
