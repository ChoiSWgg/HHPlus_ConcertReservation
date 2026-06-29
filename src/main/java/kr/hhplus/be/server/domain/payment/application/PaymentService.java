package kr.hhplus.be.server.domain.payment.application;

import kr.hhplus.be.server.domain.payment.domain.repository.PaymentRepository;
import kr.hhplus.be.server.domain.reservation.domain.repository.ReservationRepository;
import kr.hhplus.be.server.domain.wallet.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    private final ReservationRepository reservationRepository;
    private final WalletRepository walletRepository;
    private final PaymentRepository paymentRepository;

    public PaymentService(ReservationRepository reservationRepository,
                          WalletRepository walletRepository,
                          PaymentRepository paymentRepository) {
        this.reservationRepository = reservationRepository;
        this.walletRepository = walletRepository;
        this.paymentRepository = paymentRepository;
    }

    /**
     * [POST /reservations/{reservationId}/payments] 결제 요청
     *
     * 1. reservationRepository.findById(reservationId) 로 예약 조회
     *    - 없으면 CustomException(NOT_FOUND) 던지기
     * 2. reservation.isExpired() 이면 CustomException(RESERVATION_EXPIRED) 던지기 (410)
     * 3. walletRepository.findByUserId(reservation.getUserId()) 로 지갑 조회
     *    - 없으면 CustomException(NOT_FOUND) 던지기
     * 4. wallet.getBalance() < amount 이면 CustomException(INSUFFICIENT_POINTS) 던지기
     * 5. wallet.deduct(amount) → walletRepository.save(wallet) 로 포인트 차감
     * 6. Payment.of(reservationId, amount) → paymentRepository.save(payment) 로 결제 이력 저장
     * 7. reservation.confirm() → reservationRepository.save(reservation) 로 예약 확정
     * 8. PaymentResponse(payment.getId(), amount, "CONFIRMED") 반환
     */
    @Transactional
    public void processPayment(Long reservationId, Long amount) {
        // TODO: 구현
    }
}
