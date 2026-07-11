package kr.hhplus.be.server.domain.queue.controller;

import kr.hhplus.be.server.domain.queue.dto.QueueTokenRequest;
import kr.hhplus.be.server.domain.queue.dto.QueueStatusResponse;
import kr.hhplus.be.server.domain.queue.service.QueueService;
import kr.hhplus.be.server.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/queues")
@RequiredArgsConstructor
public class QueueController {

    private final QueueService queueService;

    // 대기열 토큰 발급
    @PostMapping
    public ResponseEntity<ApiResponse<QueueStatusResponse>> issueToken(
        @RequestBody QueueTokenRequest request
    ) {
        QueueStatusResponse response = queueService.issueToken(request.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response));
    }

    // 대기번호 조회
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<QueueStatusResponse>> getQueueStatus(
        @PathVariable Long userId
    ) {
        QueueStatusResponse response = queueService.getQueueStatus(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
