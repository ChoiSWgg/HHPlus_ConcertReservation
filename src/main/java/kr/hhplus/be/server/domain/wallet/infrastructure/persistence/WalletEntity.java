package kr.hhplus.be.server.domain.wallet.infrastructure.persistence;

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
}
