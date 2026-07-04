package kr.hhplus.be.server.domain.concert.service;

import kr.hhplus.be.server.domain.concert.dto.ConcertScheduleResponse;
import kr.hhplus.be.server.domain.concert.dto.SeatStatusResponse;
import kr.hhplus.be.server.domain.concert.entity.ConcertScheduleEntity;
import kr.hhplus.be.server.domain.concert.entity.SeatEntity;
import kr.hhplus.be.server.domain.concert.repository.ConcertRepository;
import kr.hhplus.be.server.domain.concert.repository.ConcertScheduleRepository;
import kr.hhplus.be.server.domain.concert.repository.SeatRepository;
import kr.hhplus.be.server.domain.reservation.domain.model.Reservation;
import kr.hhplus.be.server.domain.reservation.domain.repository.ReservationRepository;
import kr.hhplus.be.server.global.exception.CustomException;
import kr.hhplus.be.server.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConcertService {

    private final ConcertRepository concertRepository;
    private final ConcertScheduleRepository concertScheduleRepository;
    private final SeatRepository seatRepository;
    private final ReservationRepository reservationRepository;

    /**
     * [GET /concerts/{concertId}/schedules] 예약 가능 회차 목록 조회
     * <p>
     * 1. concertRepository.findById(concertId) 로 콘서트 존재 확인
     * - 없으면 CustomException(CONCERT_NOT_FOUND) 던지기
     * 2. concertScheduleRepository.findByConcertId(concertId) 로 회차 목록 조회
     * 3. 각 ConcertScheduleEntity → ConcertScheduleResponse 로 변환해서 반환
     */
    public List<ConcertScheduleResponse> getSchedules(Long concertId) {
        concertRepository.findById(concertId)
            .orElseThrow(() -> new CustomException(ErrorCode.CONCERT_NOT_FOUND));
        List<ConcertScheduleEntity> schedules = concertScheduleRepository.findByConcertId(concertId);
        return schedules.stream()
            .map(e -> new ConcertScheduleResponse(
                e.getId(), e.getDate(), e.getReservationOpenTime(), e.getReservationCloseTime()
            ))
            .collect(Collectors.toList());
    }

    /**
     * [GET /schedules/{scheduleId}/seats] 특정 회차 좌석 상태 목록 조회
     *
     * 1. concertScheduleRepository.findById(scheduleId) 로 회차 존재 확인
     *    - 없으면 CustomException(CONCERT_SCHEDULE_NOT_FOUND) 던지기
     * 2. seatRepository.findAll() 로 전체 좌석(1~50) 조회
     * 3. reservationRepository.findAllByScheduleId(scheduleId) 로 해당 회차 예약 목록 조회
     *    - Map<Long, Reservation> 으로 변환 (key: seatId)
     * 4. 각 좌석마다 예약 상태 판별:
     *    - 예약 없음 → "available"
     *    - reservation.isConfirmed() → "sold"
     *    - reservation.isHeld() && !reservation.isExpired() → "held"
     *    - 그 외 (만료된 HELD 등) → "available"
     * 5. SeatStatusResponse 리스트로 반환
     */
    public List<SeatStatusResponse> getSeatStatuses(Long scheduleId) {
        ConcertScheduleEntity concertSchedules = concertScheduleRepository.findById(scheduleId)
            .orElseThrow(() -> new CustomException(ErrorCode.CONCERT_SCHEDULE_NOT_FOUND));

        List<SeatEntity> seats = seatRepository.findAll();

        Map<Long, Reservation> reservationMap =
            reservationRepository.findAllByScheduleId(scheduleId)
            .stream()
            .collect(Collectors.toMap(Reservation::getSeatId, r -> r));

        return seats.stream()
            .map(seat -> {
                Reservation r = reservationMap.get(seat.getId());
                String status;
                if (r == null)                          status = "available";
                else if (r.isConfirmed())               status = "sold";
                else if (r.isHeld() && !r.isExpired())  status = "held";
                else                                    status = "available";
                return new SeatStatusResponse(seat.getId(), seat.getSeatNo(), status);
            })
            .collect(Collectors.toList());
    }
}
