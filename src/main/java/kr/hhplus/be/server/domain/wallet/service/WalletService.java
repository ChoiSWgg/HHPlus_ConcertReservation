package kr.hhplus.be.server.domain.wallet.service;

import kr.hhplus.be.server.domain.user.dto.ChargeResponse;
import kr.hhplus.be.server.domain.user.dto.PointResponse;
import kr.hhplus.be.server.domain.user.repository.UserRepository;
import kr.hhplus.be.server.domain.wallet.entity.WalletEntity;
import kr.hhplus.be.server.domain.wallet.repository.WalletRepository;
import kr.hhplus.be.server.global.exception.CustomException;
import kr.hhplus.be.server.global.exception.ErrorCode;
import org.springframework.stereotype.Service;

@Service
public class WalletService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;

    public WalletService(UserRepository userRepository, WalletRepository walletRepository) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
    }

    /**
     * [GET /users/{userId}/points] 포인트 잔액 조회
     * <p>
     * 1. userRepository.findById(userId) 로 유저 존재 확인
     * - 없으면 CustomException(USER_NOT_FOUND) 던지기
     * 2. walletRepository.findByUserId(userId) 로 지갑 조회
     * - 없으면 CustomException(NOT_FOUND) 던지기
     * 3. PointResponse(userId, wallet.getBalance()) 반환
     */
    public PointResponse getBalance(Long userId) {
        userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        WalletEntity walletEntity = walletRepository.findByUserId(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
        return new PointResponse(walletEntity.getBalance());
    }

    /**
     * [PATCH /users/{userId}/points] 포인트 충전
     * <p>
     * 1. amount <= 0 이면 CustomException(INVALID_AMOUNT) 던지기
     * 2. userRepository.findById(userId) 로 유저 존재 확인
     * - 없으면 CustomException(USER_NOT_FOUND) 던지기
     * 3. walletRepository.findByUserId(userId) 로 지갑 조회
     * - 없으면 CustomException(NOT_FOUND) 던지기
     * 4. wallet.charge(amount) 로 잔액 증가
     * 5. walletRepository.save(wallet) 로 저장
     * 6. ChargeResponse(amount, wallet.getBalance()) 반환
     */
    public ChargeResponse charge(Long userId, Long amount) {
        if (amount <= 0) throw new CustomException(ErrorCode.INVALID_AMOUNT);

        userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        WalletEntity walletEntity = walletRepository.findByUserId(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
        walletEntity.charge(amount);
        walletRepository.save(walletEntity);
        return new ChargeResponse(amount, walletEntity.getBalance());
    }
}
