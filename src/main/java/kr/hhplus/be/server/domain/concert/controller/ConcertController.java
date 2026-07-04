package kr.hhplus.be.server.domain.concert.controller;

import kr.hhplus.be.server.domain.concert.dto.ConcertScheduleResponse;
import kr.hhplus.be.server.domain.concert.dto.SeatStatusResponse;
import kr.hhplus.be.server.domain.concert.service.ConcertService;
import kr.hhplus.be.server.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ConcertController {

    private final ConcertService concertService;

    // 예약 가능 회차 목록 조회
    @GetMapping("/concerts/{concertId}/schedules")
    public ResponseEntity<ApiResponse<List<ConcertScheduleResponse>>> getSchedules(@PathVariable Long concertId) {
        return ResponseEntity.ok(ApiResponse.success(concertService.getSchedules(concertId)));
    }

    // 특정 회차 좌석 상태 목록 조회
    @GetMapping("/schedules/{scheduleId}/seats")
    public ResponseEntity<ApiResponse<List<SeatStatusResponse>>> getSeatStatuses(@PathVariable Long scheduleId){
        return ResponseEntity.ok(ApiResponse.success(concertService.getSeatStatuses(scheduleId)));
    }
}
