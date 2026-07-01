package kr.hhplus.be.server.domain.payment.infrastructure.persistence;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.payment.domain.model.Payment;
import kr.hhplus.be.server.global.entity.BaseEntity;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "payments")
public class PaymentEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long reservationId;

    @Column(nullable = false)
    private Long price;

    private LocalDateTime paidAt;

    // Domain -> Entity
    public static PaymentEntity from(Payment payment) {
        PaymentEntity paymentEntity = new PaymentEntity();
        paymentEntity.id = payment.getId();
        paymentEntity.reservationId = payment.getReservationId();
        paymentEntity.price = payment.getPrice();
        paymentEntity.paidAt = payment.getPaidAt();
        return paymentEntity;
    }

    // Entity -> Domain
    public Payment toDomain() {
        return Payment.reconstruct(id, reservationId, price, paidAt);
    }
}
