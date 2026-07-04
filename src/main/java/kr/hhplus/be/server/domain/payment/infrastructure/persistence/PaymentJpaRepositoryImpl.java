package kr.hhplus.be.server.domain.payment.infrastructure.persistence;

import kr.hhplus.be.server.domain.payment.domain.model.Payment;
import kr.hhplus.be.server.domain.payment.domain.repository.PaymentRepository;
import org.springframework.stereotype.Repository;

@Repository
public class PaymentJpaRepositoryImpl implements PaymentRepository {

    private final PaymentJpaRepository paymentJpaRepository;

    public PaymentJpaRepositoryImpl(PaymentJpaRepository paymentJpaRepository) {
        this.paymentJpaRepository = paymentJpaRepository;
    }

    @Override
    public Payment save(Payment payment) {

        paymentJpaRepository.save(PaymentEntity.from(payment));
        return null;
    }
}
