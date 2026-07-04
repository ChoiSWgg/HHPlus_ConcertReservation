package kr.hhplus.be.server.domain.reservation.interfaces.web.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReservationRequest {
    private Long userId;
    private Long seatId;
}
