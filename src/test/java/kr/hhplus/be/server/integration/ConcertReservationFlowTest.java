package kr.hhplus.be.server.integration;

import kr.hhplus.be.server.domain.concert.entity.ConcertEntity;
import kr.hhplus.be.server.domain.concert.entity.ConcertScheduleEntity;
import kr.hhplus.be.server.domain.concert.entity.SeatEntity;
import kr.hhplus.be.server.domain.concert.repository.ConcertJpaRepository;
import kr.hhplus.be.server.domain.concert.repository.ConcertScheduleJpaRepository;
import kr.hhplus.be.server.domain.concert.repository.SeatJpaRepository;
import kr.hhplus.be.server.domain.payment.application.PaymentFacade;
import kr.hhplus.be.server.domain.payment.interfaces.web.dto.PaymentResponse;
import kr.hhplus.be.server.domain.queue.dto.QueueStatusResponse;
import kr.hhplus.be.server.domain.queue.service.QueueService;
import kr.hhplus.be.server.domain.reservation.application.ReservationFacade;
import kr.hhplus.be.server.domain.reservation.infrastructure.persistence.ReservationEntity;
import kr.hhplus.be.server.domain.reservation.infrastructure.persistence.ReservationJpaRepository;
import kr.hhplus.be.server.domain.reservation.interfaces.web.dto.ReservationResponse;
import kr.hhplus.be.server.domain.user.entity.UserEntity;
import kr.hhplus.be.server.domain.user.repository.UserJpaRepository;
import kr.hhplus.be.server.domain.wallet.entity.WalletEntity;
import kr.hhplus.be.server.domain.wallet.repository.WalletJpaRepository;
import kr.hhplus.be.server.global.exception.CustomException;
import kr.hhplus.be.server.global.exception.ErrorCode;
import kr.hhplus.be.server.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Transactional
public class ConcertReservationFlowTest extends AbstractIntegrationTest {

    @Autowired private UserJpaRepository userJpaRepository;
    @Autowired private WalletJpaRepository walletJpaRepository;
    @Autowired private ConcertJpaRepository concertJpaRepository;
    @Autowired private ConcertScheduleJpaRepository concertScheduleJpaRepository;
    @Autowired private SeatJpaRepository seatJpaRepository;

    @Autowired private QueueService queueService;
    @Autowired private ReservationFacade reservationFacade;
    @Autowired private PaymentFacade paymentFacade;
    @Autowired private ReservationJpaRepository reservationJpaRepository;

    @Test
    void 전체_플로우_토큰발급_좌석예약_결제_성공() {

        // given
        // - 임시 유저 생성 및 DB에 저장
        UserEntity user = userJpaRepository.save(
            UserEntity.builder().name("테스트1").email("test1@test.com").password("1234").build()
        );
        // - 임시 유저의 wallet 생성 및 DB에 저장
        walletJpaRepository.save(
            WalletEntity.builder().id(user.getId()).userId(user.getId()).balance(10000L).build()
        );
        // - 콘서트 생성 및 DB에 저장
        ConcertEntity concert = concertJpaRepository.save(
            ConcertEntity.builder().title("테스트 콘서트").description("테스트 콘서트 설명").build()
        );
        // - 콘서트 스케줄 생성 및 DB에 저장
        ConcertScheduleEntity schedule = concertScheduleJpaRepository.save(
            ConcertScheduleEntity.builder()
                .concertId(concert.getId())
                .date(LocalDateTime.now().plusDays(30))
                .reservationOpenTime(LocalDateTime.now().minusDays(1))
                .reservationCloseTime(LocalDateTime.now().plusDays(29))
                .build()
        );
        // - 좌석 생성 및 DB에 저장
        SeatEntity seat = seatJpaRepository.save(SeatEntity.builder().seatNo(1).build());

        // when
        // - 토큰 발급
        queueService.issueToken(user.getId());
        ReservationResponse reservationResponse = reservationFacade.holdSeat(
            schedule.getId(), user.getId(), seat.getId()
        );
        // - 결제 및 좌석 예약
        PaymentResponse paymentResponse = paymentFacade.processPayment(
            reservationResponse.getReservationId(), user.getId(), 5000L
        );

        // then
        // - 결제가 확정됐는지
        assertThat(paymentResponse.getStatus()).isEqualTo("CONFIRMED");
        // - 결제가 콘서트값만큼 지불됐는지
        assertThat(paymentResponse.getAmount()).isEqualTo(5000L);
        // - 콘서트값만큼 지갑에서 감소했는지 확인
        WalletEntity wallet = walletJpaRepository.findByUserId(user.getId()).get();
        assertThat(wallet.getBalance()).isEqualTo(5000L); // 10000L - 5000L
    }

    @Test
    void 만료된_예약이_있으면_다른_유저가_같은_좌석_예약_성공() {

        // given
        // - 유저 2명, 콘서트 1개, 콘서트 스케줄 1개, 좌석 1개
        UserEntity userA = userJpaRepository.save(UserEntity.builder()
            .name("유저A").email("userA@test.com").password("1234").build());
        UserEntity userB = userJpaRepository.save(UserEntity.builder()
            .name("유저B").email("userB@test.com").password("1234").build());
        ConcertEntity concert = concertJpaRepository.save(ConcertEntity.builder()
            .title("테스트 콘서트").description("테스트를 위한 콘서트").build());
        ConcertScheduleEntity schedule = concertScheduleJpaRepository.save(ConcertScheduleEntity.builder()
            .concertId(concert.getId())
            .date(LocalDateTime.now().plusDays(30))
            .reservationOpenTime(LocalDateTime.now().minusDays(1))
            .reservationCloseTime(LocalDateTime.now().plusDays(29))
            .build());
        SeatEntity seat = seatJpaRepository.save(SeatEntity.builder().seatNo(1).build());

        // - 유저A는 좌석을 예약 (HELD 상태)
        queueService.issueToken(userA.getId());
        reservationFacade.holdSeat(schedule.getId(), userA.getId(), seat.getId());

        // - 유저A의 예약이 만료 시간을 넘었음 / 시간 강제 조작
        ReservationEntity expiredReservation = reservationJpaRepository
            .findByScheduleIdAndSeatId(schedule.getId(), seat.getId()).get();
        ReflectionTestUtils.setField(expiredReservation, "reservationExpiredAt",
            LocalDateTime.now().minusMinutes(10));
        reservationJpaRepository.saveAndFlush(expiredReservation);

        // when
        // - 유저B가 A가 예약중이던 좌석을 예약 시도 -> 예약 성공
        queueService.issueToken(userB.getId());
        ReservationResponse result = reservationFacade.holdSeat(
            schedule.getId(), userB.getId(), seat.getId()
        );

        // then
        // - 예약은 HELD 상태임을 검증
        assertThat(result.getStatus()).isEqualTo("HELD");
        // - 예약된 좌석 번호 검증
        assertThat(result.getSeatId()).isEqualTo(seat.getId());
    }

    @Test
    void 잔액_부족시_결제_실패() {

        // given
        // - 잔액 500원인 유저 생성
        UserEntity user = userJpaRepository.save(
            UserEntity.builder().name("유저").email("user@test.com").password("1234").build()
        );
        walletJpaRepository.save(
            WalletEntity.builder().id(user.getId()).userId(user.getId()).balance(500L).build()
        );
        ConcertEntity concert = concertJpaRepository.save(
            ConcertEntity.builder().title("테스트 콘서트").description("").build()
        );
        ConcertScheduleEntity schedule = concertScheduleJpaRepository.save(
            ConcertScheduleEntity.builder()
                .concertId(concert.getId())
                .date(LocalDateTime.now().plusDays(30))
                .reservationOpenTime(LocalDateTime.now().minusDays(1))
                .reservationCloseTime(LocalDateTime.now().plusDays(29))
                .build()
        );
        SeatEntity seat = seatJpaRepository.save(SeatEntity.builder().seatNo(1).build());

        // - 토큰 발급 및 좌석 예약
        queueService.issueToken(user.getId());
        ReservationResponse reservation = reservationFacade.holdSeat(
            schedule.getId(), user.getId(), seat.getId()
        );

        // when & then
        // - 잔액(500) < 결제금액(1000) → INSUFFICIENT_POINTS 예외
        assertThatThrownBy(() ->
            paymentFacade.processPayment(reservation.getReservationId(), user.getId(), 1000L)
        ).isInstanceOf(CustomException.class)
         .extracting("errorCode")
         .isEqualTo(ErrorCode.INSUFFICIENT_POINTS);
    }

    @Test
    void HELD_상태_좌석_중복_예약_실패() {

        // given
        // - 유저 2명, 좌석 1개
        UserEntity userA = userJpaRepository.save(UserEntity.builder()
            .name("유저A").email("userA@test.com").password("1234").build());
        UserEntity userB = userJpaRepository.save(UserEntity.builder()
            .name("유저B").email("userB@test.com").password("1234").build());
        ConcertEntity concert = concertJpaRepository.save(
            ConcertEntity.builder().title("테스트 콘서트").description("").build()
        );
        ConcertScheduleEntity schedule = concertScheduleJpaRepository.save(
            ConcertScheduleEntity.builder()
                .concertId(concert.getId())
                .date(LocalDateTime.now().plusDays(30))
                .reservationOpenTime(LocalDateTime.now().minusDays(1))
                .reservationCloseTime(LocalDateTime.now().plusDays(29))
                .build()
        );
        SeatEntity seat = seatJpaRepository.save(SeatEntity.builder().seatNo(1).build());

        // - 유저A가 이미 HELD 상태로 좌석 점유 중
        queueService.issueToken(userA.getId());
        reservationFacade.holdSeat(schedule.getId(), userA.getId(), seat.getId());

        // when & then
        // - 유저B가 같은 좌석 예약 시도 → SEAT_ALREADY_HELD 예외
        queueService.issueToken(userB.getId());
        assertThatThrownBy(() ->
            reservationFacade.holdSeat(schedule.getId(), userB.getId(), seat.getId())
        ).isInstanceOf(CustomException.class)
         .extracting("errorCode")
         .isEqualTo(ErrorCode.SEAT_ALREADY_HELD);
    }

    @Test
    void 결제_완료_후_대기열에서_제거_확인() {

        // given
        UserEntity user = userJpaRepository.save(
            UserEntity.builder().name("유저").email("user@test.com").password("1234").build()
        );
        // - 잔액 충분히 세팅
        walletJpaRepository.save(
            WalletEntity.builder().id(user.getId()).userId(user.getId()).balance(10000L).build()
        );
        ConcertEntity concert = concertJpaRepository.save(
            ConcertEntity.builder().title("테스트 콘서트").description("").build()
        );
        ConcertScheduleEntity schedule = concertScheduleJpaRepository.save(
            ConcertScheduleEntity.builder()
                .concertId(concert.getId())
                .date(LocalDateTime.now().plusDays(30))
                .reservationOpenTime(LocalDateTime.now().minusDays(1))
                .reservationCloseTime(LocalDateTime.now().plusDays(29))
                .build()
        );
        SeatEntity seat = seatJpaRepository.save(SeatEntity.builder().seatNo(1).build());

        // - 토큰 발급, 예약
        queueService.issueToken(user.getId());
        ReservationResponse reservation = reservationFacade.holdSeat(
            schedule.getId(), user.getId(), seat.getId()
        );

        // when
        // - 결제 완료 (PaymentFacade 내부에서 대기열 제거)
        paymentFacade.processPayment(reservation.getReservationId(), user.getId(), 5000L);

        // then
        // - 대기열 조회 시 USER_NOT_IN_QUEUE 예외 → 제거됐음을 확인
        assertThatThrownBy(() -> queueService.getQueueStatus(user.getId()))
            .isInstanceOf(CustomException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.USER_NOT_IN_QUEUE);
    }

    @Test
    void 토큰_발급_후_순위_조회_성공() {

        // given
        // - 유저 생성
        UserEntity user = userJpaRepository.save(
            UserEntity.builder().name("유저").email("user@test.com").password("1234").build()
        );

        // when
        // - 첫 번째 유저 토큰 발급 → rank=0 → ACTIVE
        queueService.issueToken(user.getId());
        QueueStatusResponse status = queueService.getQueueStatus(user.getId());

        // then
        // - ACTIVE 상태 확인
        assertThat(status.getStatus()).isEqualTo("ACTIVE");
        // - 대기 순번 1번 확인 (1-based)
        assertThat(status.getWaitingOrder()).isEqualTo(1L);
        // - 유저 ID 일치 확인
        assertThat(status.getUserId()).isEqualTo(user.getId());
    }
}
