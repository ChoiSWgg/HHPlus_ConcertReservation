package kr.hhplus.be.server.domain.reservation.application;

import kr.hhplus.be.server.domain.reservation.domain.model.Reservation;
import kr.hhplus.be.server.domain.reservation.domain.repository.ReservationRepository;
import kr.hhplus.be.server.domain.reservation.interfaces.web.dto.ReservationResponse;
import kr.hhplus.be.server.global.exception.CustomException;
import kr.hhplus.be.server.global.exception.ErrorCode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock ReservationRepository reservationRepository;
    @InjectMocks ReservationService reservationService;

    @Nested
    class holdSeat {

        @Test
        void 확정된_예약이_있으면_SEAT_NOT_AVAILABLE_예외() {
            Reservation confirmed = mock(Reservation.class);
            given(reservationRepository.findByScheduleAndSeat(1L, 1L)).willReturn(Optional.of(confirmed));
            given(confirmed.isConfirmed()).willReturn(true);

            assertThatThrownBy(() -> reservationService.holdSeat(1L, 1L, 1L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.SEAT_NOT_AVAILABLE);
        }

        @Test
        void 만료안된_HELD_예약이_있으면_SEAT_ALREADY_HELD_예외() {
            Reservation held = mock(Reservation.class);
            given(reservationRepository.findByScheduleAndSeat(1L, 1L)).willReturn(Optional.of(held));
            given(held.isConfirmed()).willReturn(false);
            given(held.isHeld()).willReturn(true);
            given(held.isExpired()).willReturn(false);

            assertThatThrownBy(() -> reservationService.holdSeat(1L, 1L, 1L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.SEAT_ALREADY_HELD);
        }

        @Test
        void 예약없으면_정상_임시배정_후_ReservationResponse_반환() {
            Reservation saved = mock(Reservation.class);
            given(reservationRepository.findByScheduleAndSeat(1L, 1L)).willReturn(Optional.empty());
            given(reservationRepository.save(any())).willReturn(saved);
            given(saved.getId()).willReturn(10L);
            given(saved.getStatus()).willReturn("HELD");
            given(saved.getScheduleId()).willReturn(1L);
            given(saved.getSeatId()).willReturn(1L);
            given(saved.getReservationExpiredAt()).willReturn(LocalDateTime.now().plusMinutes(5));

            ReservationResponse result = reservationService.holdSeat(1L, 1L, 1L);

            assertThat(result.getStatus()).isEqualTo("HELD");
            assertThat(result.getReservationId()).isEqualTo(10L);
        }
    }
}
