package kr.hhplus.be.server.domain.payment.application;

import kr.hhplus.be.server.domain.payment.domain.model.Payment;
import kr.hhplus.be.server.domain.payment.domain.repository.PaymentRepository;
import kr.hhplus.be.server.domain.payment.interfaces.web.dto.PaymentResponse;
import kr.hhplus.be.server.domain.reservation.domain.model.Reservation;
import kr.hhplus.be.server.domain.reservation.domain.repository.ReservationRepository;
import kr.hhplus.be.server.domain.wallet.entity.WalletEntity;
import kr.hhplus.be.server.domain.wallet.repository.WalletRepository;
import kr.hhplus.be.server.global.exception.CustomException;
import kr.hhplus.be.server.global.exception.ErrorCode;
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
     * <p>
     * 1. reservationRepository.findById(reservationId) 로 예약 조회
     * - 없으면 CustomException(NOT_FOUND) 던지기
     * 2. reservation.isExpired() 이면 CustomException(RESERVATION_EXPIRED) 던지기 (410)
     * 3. walletRepository.findByUserId(reservation.getUserId()) 로 지갑 조회
     * - 없으면 CustomException(NOT_FOUND) 던지기
     * 4. wallet.getBalance() < amount 이면 CustomException(INSUFFICIENT_POINTS) 던지기
     * 5. wallet.deduct(amount) → walletRepository.save(wallet) 로 포인트 차감
     * 6. Payment.of(reservationId, amount) → paymentRepository.save(payment) 로 결제 이력 저장
     * 7. reservation.confirm() → reservationRepository.save(reservation) 로 예약 확정
     * 8. PaymentResponse(payment.getId(), amount, "CONFIRMED") 반환
     */
    @Transactional
    public PaymentResponse processPayment(Long reservationId, Long amount) {
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        if (reservation.isExpired()) throw new CustomException(ErrorCode.RESERVATION_EXPIRED);

        WalletEntity wallet = walletRepository.findByUserId(reservation.getUserId())
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
        // 잔액 감소
        if (wallet.getBalance() < amount) throw new CustomException(ErrorCode.INSUFFICIENT_POINTS);
        wallet.deduct(amount);
        walletRepository.save(wallet);
        // 결제 이력 저장
        Payment payment = Payment.of(reservationId, amount);
        Payment savedPayment = paymentRepository.save(payment);

        // 예약 확정
        reservation.confirm();
        reservationRepository.save(reservation);
        return new PaymentResponse(savedPayment.getId(), amount, "CONFIRMED");
    }
}
