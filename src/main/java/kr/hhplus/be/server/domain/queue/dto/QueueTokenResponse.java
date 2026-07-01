package kr.hhplus.be.server.domain.queue.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class QueueTokenResponse {
    private Long userId;
    private String token;
    private String status;
    private long waitingOrder;
}
