package kr.hhplus.be.server.domain.wallet.repository;

import kr.hhplus.be.server.domain.wallet.entity.WalletEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class WalletJpaRepositoryImpl implements WalletRepository {

    private final WalletJpaRepository walletJpaRepository;

    @Override
    public Optional<WalletEntity> findByUserId(Long userId) {
        return walletJpaRepository.findByUserId(userId);
    }

    @Override
    public WalletEntity save(WalletEntity wallet) {
        return walletJpaRepository.save(wallet);
    }
}
