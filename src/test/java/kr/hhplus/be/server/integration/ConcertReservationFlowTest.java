package kr.hhplus.be.server.integration;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.domain.concert.entity.ConcertEntity;
import kr.hhplus.be.server.domain.concert.entity.ConcertScheduleEntity;
import kr.hhplus.be.server.domain.concert.entity.SeatEntity;
import kr.hhplus.be.server.domain.concert.repository.ConcertJpaRepository;
import kr.hhplus.be.server.domain.concert.repository.ConcertScheduleJpaRepository;
import kr.hhplus.be.server.domain.concert.repository.SeatJpaRepository;
import kr.hhplus.be.server.domain.payment.application.PaymentFacade;
import kr.hhplus.be.server.domain.payment.infrastructure.persistence.PaymentJpaRepository;
import kr.hhplus.be.server.domain.payment.interfaces.web.dto.PaymentResponse;
import kr.hhplus.be.server.domain.queue.dto.QueueTokenResponse;
import kr.hhplus.be.server.domain.queue.repository.QueueRedisRepository;
import kr.hhplus.be.server.domain.queue.service.QueueService;
import kr.hhplus.be.server.domain.reservation.application.ReservationService;
import kr.hhplus.be.server.domain.reservation.interfaces.web.dto.ReservationResponse;
import kr.hhplus.be.server.domain.user.entity.UserEntity;
import kr.hhplus.be.server.domain.user.repository.UserJpaRepository;
import kr.hhplus.be.server.domain.wallet.entity.WalletEntity;
import kr.hhplus.be.server.domain.wallet.repository.WalletJpaRepository;
import kr.hhplus.be.server.support.AbstractIntegrationTest;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@Transactional
@RequiredArgsConstructor
public class ConcertReservationFlowTest extends AbstractIntegrationTest {

    @Autowired private UserJpaRepository userJpaRepository;
    @Autowired private WalletJpaRepository walletJpaRepository;
    @Autowired private ConcertJpaRepository concertJpaRepository;
    @Autowired private ConcertScheduleJpaRepository concertScheduleJpaRepository;
    @Autowired private SeatJpaRepository seatJpaRepository;

    @Autowired private QueueService queueService;
    @Autowired private ReservationService reservationService;
    @Autowired private PaymentFacade paymentFacade;

    @Test
    void 토큰발급_좌석예약_결제_전체_플로우_성공() {

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
        ReservationResponse reservationResponse = reservationService.holdSeat(
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

}
