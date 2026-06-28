package kr.hhplus.be.server.domain.payment.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringPaymentJpa extends JpaRepository<PaymentEntity, Long> {
}
