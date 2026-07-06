package kr.hhplus.be.server.domain.concert.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.global.entity.BaseEntity;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "seats")
public class SeatEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer seatNo;

    @Builder
    private SeatEntity(Integer seatNo) {
        this.seatNo = seatNo;
    }
}
