package kr.hhplus.be.server.domain.concert.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.global.entity.BaseEntity;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "concerts")
public class ConcertEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Builder
    private ConcertEntity(String title, String description) {
        this.title = title;
        this.description = description;
    }
}
