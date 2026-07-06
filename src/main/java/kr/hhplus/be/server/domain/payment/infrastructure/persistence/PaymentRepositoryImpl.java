package kr.hhplus.be.server.domain.payment.infrastructure.persistence;

import kr.hhplus.be.server.domain.payment.domain.model.Payment;
import kr.hhplus.be.server.domain.payment.domain.repository.PaymentRepository;
import org.springframework.stereotype.Repository;

@Repository
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentJpaRepository paymentJpaRepository;

    public PaymentRepositoryImpl(PaymentJpaRepository paymentJpaRepository) {
        this.paymentJpaRepository = paymentJpaRepository;
    }

    @Override
    public Payment save(Payment payment) {
        return paymentJpaRepository.save(PaymentEntity.from(payment)).toDomain();
    }
}
