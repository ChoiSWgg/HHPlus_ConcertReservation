package kr.hhplus.be.server.domain.wallet.service;

import kr.hhplus.be.server.domain.user.repository.UserRepository;
import kr.hhplus.be.server.domain.wallet.repository.WalletRepository;
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
     *
     * 1. userRepository.findById(userId) 로 유저 존재 확인
     *    - 없으면 CustomException(USER_NOT_FOUND) 던지기
     * 2. walletRepository.findByUserId(userId) 로 지갑 조회
     *    - 없으면 CustomException(NOT_FOUND) 던지기
     * 3. PointResponse(userId, wallet.getBalance()) 반환
     */
    public void getBalance(Long userId) {
        // TODO: 구현
    }

    /**
     * [PATCH /users/{userId}/points] 포인트 충전
     *
     * 1. amount <= 0 이면 CustomException(INVALID_AMOUNT) 던지기
     * 2. userRepository.findById(userId) 로 유저 존재 확인
     *    - 없으면 CustomException(USER_NOT_FOUND) 던지기
     * 3. walletRepository.findByUserId(userId) 로 지갑 조회
     *    - 없으면 CustomException(NOT_FOUND) 던지기
     * 4. wallet.charge(amount) 로 잔액 증가
     * 5. walletRepository.save(wallet) 로 저장
     * 6. ChargeResponse(amount, wallet.getBalance()) 반환
     */
    public void charge(Long userId, Long amount) {
        // TODO: 구현
    }
}
