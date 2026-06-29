package kr.hhplus.be.server.domain.reservation.infrastructure.persistence;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.reservation.domain.model.Reservation;
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
    
    // 도메인 모델 -> 엔티티 변환 (DB에 저장할 때 사용)
    public static ReservationEntity from(Reservation reservation) {

        ReservationEntity reservationEntity = new ReservationEntity();
        reservationEntity.id = reservation.getId();
        reservationEntity.userId = reservation.getUserId();
        reservationEntity.scheduleId = reservation.getScheduleId();
        reservationEntity.seatId = reservation.getSeatId();
        reservationEntity.status = reservation.getStatus();
        reservationEntity.reservedAt = reservation.getReservedAt();
        reservationEntity.reservationExpiredAt = reservation.getReservationExpiredAt();
        reservationEntity.confirmedAt = reservation.getConfirmedAt();
        return reservationEntity;
    }

    // 엔티티 -> 도메인 모델 변환 (DB에서 조회 후 사용)
    public Reservation toDomain() {
        return Reservation.reconstruct(id, userId, scheduleId, seatId, status,
            reservedAt, reservationExpiredAt, confirmedAt);
    }
}
