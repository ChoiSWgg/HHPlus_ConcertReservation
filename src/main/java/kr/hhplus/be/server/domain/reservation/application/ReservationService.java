package kr.hhplus.be.server.domain.reservation.application;

import kr.hhplus.be.server.domain.reservation.domain.repository.ReservationRepository;
import org.springframework.stereotype.Service;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;

    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    /**
     * [POST /schedules/{scheduleId}/reservations] 좌석 임시 배정 (5분간 HELD)
     *
     * 1. reservationRepository.findByScheduleAndSeat(scheduleId, seatId) 로 기존 예약 조회
     * 2. 기존 예약이 있으면:
     *    - reservation.isConfirmed() → CustomException(SEAT_NOT_AVAILABLE) 던지기
     *    - reservation.isHeld() && !reservation.isExpired() → CustomException(SEAT_ALREADY_HELD) 던지기
     *    - 만료된 HELD → 통과 (새 예약 허용)
     * 3. Reservation.hold(userId, scheduleId, seatId) 로 새 예약 도메인 객체 생성
     * 4. reservationRepository.save(reservation) 로 저장
     * 5. ReservationResponse(id, status, scheduleId, seatId, reservationExpiredAt) 반환
     */
    public void holdSeat(Long scheduleId, Long userId, Long seatId) {
        // TODO: 구현
    }
}
