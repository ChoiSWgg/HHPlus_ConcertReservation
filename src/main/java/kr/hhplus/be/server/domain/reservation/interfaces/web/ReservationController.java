package kr.hhplus.be.server.domain.reservation.interfaces.web;

import kr.hhplus.be.server.domain.reservation.application.ReservationService;
import kr.hhplus.be.server.domain.reservation.interfaces.web.dto.ReservationRequest;
import kr.hhplus.be.server.domain.reservation.interfaces.web.dto.ReservationResponse;
import kr.hhplus.be.server.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    // 좌석 임시 배정
    @PostMapping("/schedules/{scheduleId}/reservations")
    public ResponseEntity<ApiResponse<ReservationResponse>> holdSeat(
        @PathVariable Long scheduleId,
        @RequestBody ReservationRequest request
    ) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(
                ApiResponse.success(
                    reservationService.holdSeat(scheduleId, request.getUserId(), request.getSeatId())
                )
            );
    }
}
