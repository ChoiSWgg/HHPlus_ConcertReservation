package kr.hhplus.be.server.domain.concert.service;

import kr.hhplus.be.server.domain.concert.dto.ConcertScheduleResponse;
import kr.hhplus.be.server.domain.concert.dto.SeatStatusResponse;
import kr.hhplus.be.server.domain.concert.entity.ConcertEntity;
import kr.hhplus.be.server.domain.concert.entity.ConcertScheduleEntity;
import kr.hhplus.be.server.domain.concert.entity.SeatEntity;
import kr.hhplus.be.server.domain.concert.repository.ConcertRepository;
import kr.hhplus.be.server.domain.concert.repository.ConcertScheduleRepository;
import kr.hhplus.be.server.domain.concert.repository.SeatRepository;
import kr.hhplus.be.server.domain.reservation.domain.repository.ReservationRepository;
import kr.hhplus.be.server.global.exception.CustomException;
import kr.hhplus.be.server.global.exception.ErrorCode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class ConcertServiceTest {

    @Mock ConcertRepository concertRepository;
    @Mock ConcertScheduleRepository concertScheduleRepository;
    @Mock SeatRepository seatRepository;
    @Mock ReservationRepository reservationRepository;
    @InjectMocks ConcertService concertService;

    @Nested
    class getSchedules {

        @Test
        void 콘서트가_없으면_CONCERT_NOT_FOUND_예외() {
            given(concertRepository.findById(1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> concertService.getSchedules(1L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.CONCERT_NOT_FOUND);
        }

        @Test
        void 정상_조회시_ConcertScheduleResponse_목록_반환() {
            ConcertScheduleEntity schedule = mock(ConcertScheduleEntity.class);
            given(concertRepository.findById(1L)).willReturn(Optional.of(mock(ConcertEntity.class)));
            given(concertScheduleRepository.findByConcertId(1L)).willReturn(List.of(schedule));
            given(schedule.getId()).willReturn(10L);
            given(schedule.getDate()).willReturn(LocalDateTime.now());
            given(schedule.getReservationOpenTime()).willReturn(LocalDateTime.now());
            given(schedule.getReservationCloseTime()).willReturn(LocalDateTime.now());

            List<ConcertScheduleResponse> result = concertService.getSchedules(1L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getScheduleId()).isEqualTo(10L);
        }
    }

    @Nested
    class getSeatStatuses {

        @Test
        void 회차가_없으면_CONCERT_SCHEDULE_NOT_FOUND_예외() {
            given(concertScheduleRepository.findById(1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> concertService.getSeatStatuses(1L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.CONCERT_SCHEDULE_NOT_FOUND);
        }

        @Test
        void 예약없는_좌석은_available_상태() {
            SeatEntity seat = mock(SeatEntity.class);
            given(concertScheduleRepository.findById(1L)).willReturn(Optional.of(mock(ConcertScheduleEntity.class)));
            given(seatRepository.findAll()).willReturn(List.of(seat));
            given(reservationRepository.findAllByScheduleId(1L)).willReturn(List.of());
            given(seat.getId()).willReturn(1L);
            given(seat.getSeatNo()).willReturn(1);

            List<SeatStatusResponse> result = concertService.getSeatStatuses(1L);

            assertThat(result.get(0).getStatus()).isEqualTo("available");
        }
    }
}