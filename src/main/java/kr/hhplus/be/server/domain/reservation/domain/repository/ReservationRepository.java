package kr.hhplus.be.server.domain.reservation.domain.repository;

import kr.hhplus.be.server.domain.reservation.domain.model.Reservation;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

    // 예약 저장 (신규 생성 및 상태 변경 모두 사용)
    Reservation save(Reservation reservation);

    // 예약 단건 조회 (결제 시 사용)
    Optional<Reservation> findById(Long reservationId);

    // 특정 회차의 전체 예약 목록 조회 (좌석 상태 판별 시 사용)
    List<Reservation> findAllByScheduleId(Long scheduleId);

    // 특정 회차 + 좌석 조합으로 예약 조회 (좌석 임시 배정 시 중복 확인용)
    Optional<Reservation> findByScheduleAndSeat(Long scheduleId, Long seatId);

}
