package kr.hhplus.be.server.domain.reservation.interfaces.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ReservationResponse {
    private Long reservationId;
    private String status;
    private Long scheduleId;
    private Long seatId;
    private LocalDateTime holdExpireTime;
}
