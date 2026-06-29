package kr.hhplus.be.server.domain.wallet.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.global.entity.BaseEntity;
import lombok.Getter;

@Getter
@Entity
@Table(name = "wallets")
public class WalletEntity extends BaseEntity {

    @Id
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long balance;

    // 포인트 충전
    public void charge(Long amount) {
        this.balance += amount;
    }

    // 포인트 차감
    public void deduct(Long amount) {
        this.balance -= amount;
    }
}
