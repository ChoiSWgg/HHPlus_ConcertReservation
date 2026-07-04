package kr.hhplus.be.server.domain.payment.application;

import kr.hhplus.be.server.domain.payment.domain.repository.PaymentRepository;
import kr.hhplus.be.server.domain.payment.interfaces.web.dto.PaymentResponse;
import kr.hhplus.be.server.domain.reservation.domain.model.Reservation;
import kr.hhplus.be.server.domain.reservation.domain.repository.ReservationRepository;
import kr.hhplus.be.server.domain.wallet.entity.WalletEntity;
import kr.hhplus.be.server.domain.wallet.repository.WalletRepository;
import kr.hhplus.be.server.global.exception.CustomException;
import kr.hhplus.be.server.global.exception.ErrorCode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock ReservationRepository reservationRepository;
    @Mock WalletRepository walletRepository;
    @Mock PaymentRepository paymentRepository;
    @InjectMocks PaymentService paymentService;

    @Nested
    class processPayment {

        @Test
        void 예약이_없으면_NOT_FOUND_예외() {
            given(reservationRepository.findById(1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> paymentService.processPayment(1L, 1000L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.NOT_FOUND);
        }

        @Test
        void 예약이_만료되면_RESERVATION_EXPIRED_예외() {
            Reservation reservation = mock(Reservation.class);
            given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));
            given(reservation.isExpired()).willReturn(true);

            assertThatThrownBy(() -> paymentService.processPayment(1L, 1000L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.RESERVATION_EXPIRED);
        }

        @Test
        void 지갑이_없으면_NOT_FOUND_예외() {
            Reservation reservation = mock(Reservation.class);
            given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));
            given(reservation.isExpired()).willReturn(false);
            given(reservation.getUserId()).willReturn(1L);
            given(walletRepository.findByUserId(1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> paymentService.processPayment(1L, 1000L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.NOT_FOUND);
        }

        @Test
        void 잔액이_부족하면_INSUFFICIENT_POINTS_예외() {
            Reservation reservation = mock(Reservation.class);
            WalletEntity wallet = mock(WalletEntity.class);
            given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));
            given(reservation.isExpired()).willReturn(false);
            given(reservation.getUserId()).willReturn(1L);
            given(walletRepository.findByUserId(1L)).willReturn(Optional.of(wallet));
            given(wallet.getBalance()).willReturn(500L);

            assertThatThrownBy(() -> paymentService.processPayment(1L, 1000L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INSUFFICIENT_POINTS);
        }

        @Test
        void 정상_결제시_PaymentResponse_반환() {
            Reservation reservation = mock(Reservation.class);
            WalletEntity wallet = mock(WalletEntity.class);
            kr.hhplus.be.server.domain.payment.domain.model.Payment savedPayment =
                mock(kr.hhplus.be.server.domain.payment.domain.model.Payment.class);

            given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));
            given(reservation.isExpired()).willReturn(false);
            given(reservation.getUserId()).willReturn(1L);
            given(walletRepository.findByUserId(1L)).willReturn(Optional.of(wallet));
            given(wallet.getBalance()).willReturn(5000L);
            given(walletRepository.save(wallet)).willReturn(wallet);
            given(paymentRepository.save(any())).willReturn(savedPayment);
            given(reservationRepository.save(any())).willReturn(reservation);
            given(savedPayment.getId()).willReturn(100L);

            PaymentResponse result = paymentService.processPayment(1L, 1000L);

            assertThat(result.getPaymentId()).isEqualTo(100L);
            assertThat(result.getAmount()).isEqualTo(1000L);
            assertThat(result.getStatus()).isEqualTo("CONFIRMED");
        }
    }
}
