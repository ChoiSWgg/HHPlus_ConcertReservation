package kr.hhplus.be.server.domain.reservation.infrastructure.persistence;

import jakarta.persistence.*;
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
    private Long reservationsId;

    @Column(nullable = false)
    private Integer price;

    private LocalDateTime paidAt;
}
