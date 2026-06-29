package kr.hhplus.be.server.domain.reservation.domain.model;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class Reservation {

    private Long id;
    private Long userId;
    private Long scheduleId;
    private Long seatId;
    private String status;
    private LocalDateTime reservedAt;
    private LocalDateTime reservationExpiredAt;
    private LocalDateTime confirmedAt; // 결제 완료시.

    private Reservation() {}

    // 예약을 만들어, 특정 회차의 좌석을 5분간 임시 점유(HELD)합니다.
    public static Reservation hold(Long userId, Long scheduleId, Long seatId) {
        Reservation reservation = new Reservation();
        reservation.userId = userId;
        reservation.scheduleId = scheduleId;
        reservation.seatId = seatId;
        reservation.status = "HELD";
        reservation.reservedAt = LocalDateTime.now();
        reservation.reservationExpiredAt = LocalDateTime.now().plusMinutes(5L); // 5분간 임시점유
        return reservation;
    }

    // 엔터티 -> 도메인 역매핑용
    public static Reservation reconstruct(Long id, Long userId, Long scheduleId, Long seatId, String status,
                                          LocalDateTime reservedAt, LocalDateTime reservationExpiredAt, LocalDateTime confirmedAt) {
        Reservation reservation = new Reservation();
        reservation.id = id;
        reservation.userId = userId;
        reservation.scheduleId = scheduleId;
        reservation.seatId = seatId;
        reservation.status = status;
        reservation.reservedAt = reservedAt;
        reservation.reservationExpiredAt = reservationExpiredAt;
        reservation.confirmedAt = confirmedAt;
        return reservation;
    }

    // 비즈니스 로직
    // reservationExpiredAt이 이미 지났는지
    public boolean isExpired() {
        return reservationExpiredAt != null &&
            LocalDateTime.now().isAfter(reservationExpiredAt);
    }

    // status.equals("HELD")
    public boolean isHeld() { return "HELD".equals(status); }

    // status.equals("CONFIRMED")
    public boolean isConfirmed() { return "CONFIRMED".equals(status); }

    // status = "CONFIRMED", confirmedAt = now
    public void confirm() { status = "CONFIRMED"; confirmedAt = LocalDateTime.now();}

}
