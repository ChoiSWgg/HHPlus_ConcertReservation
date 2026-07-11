package kr.hhplus.be.server.domain.wallet.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.global.entity.BaseEntity;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "wallets")
public class WalletEntity extends BaseEntity {

    @Id
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long balance;

    // id는 @GeneratedValue 없으므로 빌더에 포함
    @Builder
    private WalletEntity(Long id, Long userId, Long balance) {
        this.id = id;
        this.userId = userId;
        this.balance = balance;
    }

    // 포인트 충전
    public void charge(Long amount) {
        this.balance += amount;
    }

    // 포인트 차감
    public void deduct(Long amount) {
        this.balance -= amount;
    }
}
