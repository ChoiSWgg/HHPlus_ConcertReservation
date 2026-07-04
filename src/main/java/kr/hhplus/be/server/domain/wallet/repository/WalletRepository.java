package kr.hhplus.be.server.domain.wallet.repository;

import kr.hhplus.be.server.domain.wallet.entity.WalletEntity;

import java.util.Optional;

public interface WalletRepository {

    // 유저 ID로 지갑 조회 (포인트 조회/충전/차감 시 사용)
    Optional<WalletEntity> findByUserId(Long userId);

    // 지갑 저장 (충전/차감 후 상태 반영)
    WalletEntity save(WalletEntity wallet);
}
