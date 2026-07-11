package kr.hhplus.be.server.domain.reservation.application;

import kr.hhplus.be.server.domain.reservation.domain.model.Reservation;
import kr.hhplus.be.server.domain.reservation.domain.repository.ReservationRepository;
import kr.hhplus.be.server.domain.reservation.interfaces.web.dto.ReservationResponse;
import kr.hhplus.be.server.global.exception.CustomException;
import kr.hhplus.be.server.global.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
     * - reservation.isConfirmed() → CustomException(SEAT_NOT_AVAILABLE) 던지기
     * - reservation.isHeld() && !reservation.isExpired() → CustomException(SEAT_ALREADY_HELD) 던지기
     * - 만료된 HELD → 통과 (새 예약 허용)
     * 3. Reservation.hold(userId, scheduleId, seatId) 로 새 예약 도메인 객체 생성
     * 4. reservationRepository.save(reservation) 로 저장
     * 5. ReservationResponse(id, status, scheduleId, seatId, reservationExpiredAt) 반환
     */
    public ReservationResponse holdSeat(Long scheduleId, Long userId, Long seatId) {
        Optional<Reservation> existing = reservationRepository.findByScheduleAndSeat(scheduleId, seatId);

        // 기존 예약 있음
        if (existing.isPresent()) {
            Reservation prev = existing.get();
            if (prev.isConfirmed()) throw new CustomException(ErrorCode.SEAT_NOT_AVAILABLE);
            if (prev.isHeld() && !prev.isExpired()) throw new CustomException(ErrorCode.SEAT_ALREADY_HELD);

            // 만료된 HELD: 새 행이 아닌, 기존 행을 UPDATE
            //              (INSERT 시 unique constraint 위반 방지)
            Reservation reservation = Reservation.rehold(prev.getId(), userId, scheduleId, seatId);
            Reservation saved = reservationRepository.save(reservation);
            return new ReservationResponse(
                saved.getId(), saved.getStatus(), saved.getScheduleId(),
                saved.getSeatId(), saved.getReservationExpiredAt()
            );
        }

        // 기존 예약 없음: 새 행 INSERT
        Reservation reservation = Reservation.hold(userId, scheduleId, seatId);
        Reservation saved = reservationRepository.save(reservation);
        return new ReservationResponse(
            saved.getId(), saved.getStatus(), saved.getScheduleId(),
            saved.getSeatId(), saved.getReservationExpiredAt()
        );
    }
}
