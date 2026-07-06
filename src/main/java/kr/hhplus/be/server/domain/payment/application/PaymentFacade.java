package kr.hhplus.be.server.domain.payment.application;

import kr.hhplus.be.server.domain.payment.interfaces.web.dto.PaymentResponse;
import kr.hhplus.be.server.domain.queue.service.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentFacade {
    private final PaymentService paymentService;
    private final QueueService queueService;

    // 결제 처리 후 대기열 제거까지 조율 (PaymentService는 결제만, 대기열 제거는 여기서)
    public PaymentResponse processPayment(Long reservationId, Long userId, Long amount) {
        PaymentResponse response = paymentService.processPayment(reservationId, amount);
        queueService.removeFromQueue(userId); // 결제 완료 → 대기열에서 제거
        return response;
    }
}
