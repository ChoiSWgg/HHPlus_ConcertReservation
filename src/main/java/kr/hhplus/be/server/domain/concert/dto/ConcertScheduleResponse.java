package kr.hhplus.be.server.domain.concert.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ConcertScheduleResponse {
    private Long scheduleId;
    private LocalDateTime date;
    private LocalDateTime reservationOpenTime;
    private LocalDateTime reservationCloseTime;
}
