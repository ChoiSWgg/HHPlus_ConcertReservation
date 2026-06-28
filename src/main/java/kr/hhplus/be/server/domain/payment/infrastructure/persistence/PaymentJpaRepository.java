package kr.hhplus.be.server.domain.payment.infrastructure.persistence;

import kr.hhplus.be.server.domain.payment.domain.repository.PaymentRepository;
import org.springframework.stereotype.Repository;

@Repository
public class PaymentJpaRepository implements PaymentRepository {

    private final SpringPaymentJpa springPaymentJpa;

    public PaymentJpaRepository(SpringPaymentJpa springPaymentJpa) {
        this.springPaymentJpa = springPaymentJpa;
    }
}
