package kr.hhplus.be.server.domain.reservation.application;


import kr.hhplus.be.server.domain.queue.dto.QueueStatusResponse;
import kr.hhplus.be.server.domain.queue.service.QueueService;
import kr.hhplus.be.server.domain.reservation.interfaces.web.dto.ReservationResponse;
import kr.hhplus.be.server.global.exception.CustomException;
import kr.hhplus.be.server.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReservationFacade {

    private final ReservationService reservationService;
    private final QueueService queueService;

    public ReservationResponse holdSeat(Long scheduleId, Long userId, Long seatId) {
        // 대기열 토큰 검증
        QueueStatusResponse queueStatus = queueService.getQueueStatus(userId);
        if (!("ACTIVE".equals(queueStatus.getStatus())))
            throw new CustomException(ErrorCode.QUEUE_NOT_ACTIVE);
        return reservationService.holdSeat(scheduleId, userId, seatId);
    }
}
