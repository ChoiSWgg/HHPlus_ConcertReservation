package kr.hhplus.be.server.domain.reservation.infrastructure.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "reservations")
public class ReservationEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
