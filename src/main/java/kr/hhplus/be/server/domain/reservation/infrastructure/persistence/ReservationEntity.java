package kr.hhplus.be.server.domain.reservation.infrastructure.persistence;

import jakarta.persistence.*;
import kr.hhplus.be.server.global.entity.BaseEntity;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "reservations",
        uniqueConstraints = @UniqueConstraint(
                name = "reservations_unique_schedule_seat",
                columnNames = {"schedule_id", "seat_id"}
        ))
public class ReservationEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "schedule_id", nullable = false)
    private Long scheduleId;

    @Column(name = "seat_id", nullable = false)
    private Long seatId;

    @Column(nullable = false, length = 50)
    private String status;

    private LocalDateTime reservedAt;

    private LocalDateTime reservationExpiredAt;

    private LocalDateTime confirmedAt;
}
