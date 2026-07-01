package kr.hhplus.be.server.domain.concert.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SeatStatusResponse {
    private Long seatId;
    private int seatNo;
    private String status;
}
