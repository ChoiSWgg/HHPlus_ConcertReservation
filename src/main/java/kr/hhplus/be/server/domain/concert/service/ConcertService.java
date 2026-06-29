package kr.hhplus.be.server.domain.concert.service;

import kr.hhplus.be.server.domain.concert.repository.ConcertRepository;
import kr.hhplus.be.server.domain.reservation.domain.repository.ReservationRepository;
import org.springframework.stereotype.Service;

@Service
public class ConcertService {

    private final ConcertRepository concertRepository;
    private final ReservationRepository reservationRepository;

    public ConcertService(ConcertRepository concertRepository, ReservationRepository reservationRepository) {
        this.concertRepository = concertRepository;
        this.reservationRepository = reservationRepository;
    }

    /**
     * [GET /concerts/{concertId}/schedules] 예약 가능 회차 목록 조회
     *
     * 1. concertRepository.findById(concertId) 로 콘서트 존재 확인
     *    - 없으면 CustomException(CONCERT_NOT_FOUND) 던지기
     * 2. concertRepository.findSchedulesByConcertId(concertId) 로 회차 목록 조회
     * 3. 각 ConcertScheduleEntity → ConcertScheduleResponse 로 변환해서 반환
     */
    public void getSchedules(Long concertId) {
        // TODO: 구현
    }

    /**
     * [GET /schedules/{scheduleId}/seats] 특정 회차 좌석 상태 목록 조회
     *
     * 1. concertRepository.findScheduleById(scheduleId) 로 회차 존재 확인
     *    - 없으면 CustomException(CONCERT_SCHEDULE_NOT_FOUND) 던지기
     * 2. concertRepository.findAllSeats() 로 전체 좌석(1~50) 조회
     * 3. reservationRepository.findAllByScheduleId(scheduleId) 로 해당 회차 예약 목록 조회
     *    - Map<Long, Reservation> 으로 변환 (key: seatId)
     * 4. 각 좌석마다 예약 상태 판별:
     *    - 예약 없음 → "available"
     *    - reservation.isConfirmed() → "sold"
     *    - reservation.isHeld() && !reservation.isExpired() → "held"
     *    - 그 외 (만료된 HELD 등) → "available"
     * 5. SeatStatusResponse 리스트로 반환
     */
    public void getSeats(Long scheduleId) {
        // TODO: 구현
    }
}
