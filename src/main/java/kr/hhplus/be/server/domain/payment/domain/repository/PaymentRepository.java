package kr.hhplus.be.server.domain.payment.domain.repository;

import kr.hhplus.be.server.domain.payment.domain.model.Payment;

public interface PaymentRepository {

    // 결제 저장
    Payment save(Payment payment);
}
