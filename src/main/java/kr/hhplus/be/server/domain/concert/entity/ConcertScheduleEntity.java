package kr.hhplus.be.server.domain.concert.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.global.entity.BaseEntity;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "concert_schedules")
public class ConcertScheduleEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long concertId;

    @Column(nullable = false)
    private LocalDateTime date;

    @Column(nullable = false)
    private LocalDateTime reservationOpenTime;

    @Column(nullable = false)
    private LocalDateTime reservationCloseTime;

    @Builder
    private ConcertScheduleEntity(Long concertId, LocalDateTime date,
                                   LocalDateTime reservationOpenTime, LocalDateTime reservationCloseTime) {
        this.concertId = concertId;
        this.date = date;
        this.reservationOpenTime = reservationOpenTime;
        this.reservationCloseTime = reservationCloseTime;
    }
}
