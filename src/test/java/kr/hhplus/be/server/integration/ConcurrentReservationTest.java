package kr.hhplus.be.server.integration;

import kr.hhplus.be.server.domain.concert.entity.ConcertEntity;
import kr.hhplus.be.server.domain.concert.entity.ConcertScheduleEntity;
import kr.hhplus.be.server.domain.concert.entity.SeatEntity;
import kr.hhplus.be.server.domain.concert.repository.ConcertJpaRepository;
import kr.hhplus.be.server.domain.concert.repository.ConcertScheduleJpaRepository;
import kr.hhplus.be.server.domain.concert.repository.SeatJpaRepository;
import kr.hhplus.be.server.domain.payment.application.PaymentFacade;
import kr.hhplus.be.server.domain.queue.service.QueueService;
import kr.hhplus.be.server.domain.reservation.application.ReservationFacade;
import kr.hhplus.be.server.domain.reservation.infrastructure.persistence.ReservationJpaRepository;
import kr.hhplus.be.server.domain.reservation.interfaces.web.dto.ReservationResponse;
import kr.hhplus.be.server.domain.user.entity.UserEntity;
import kr.hhplus.be.server.domain.user.repository.UserJpaRepository;
import kr.hhplus.be.server.domain.wallet.entity.WalletEntity;
import kr.hhplus.be.server.domain.wallet.repository.WalletJpaRepository;
import kr.hhplus.be.server.domain.wallet.service.WalletService;
import kr.hhplus.be.server.support.AbstractIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class ConcurrentReservationTest extends AbstractIntegrationTest {

    @Autowired
    private UserJpaRepository userJpaRepository;
    @Autowired
    private WalletJpaRepository walletJpaRepository;

    private final List<Long> userIds = new ArrayList<>();
    @Autowired private QueueService queueService;
    @Autowired private ConcertJpaRepository concertJpaRepository;
    @Autowired private ConcertScheduleJpaRepository concertScheduleJpaRepository;
    @Autowired private SeatJpaRepository seatJpaRepository;
    @Autowired private ReservationJpaRepository reservationJpaRepository;
    @Autowired private ReservationFacade reservationFacade;
    @Autowired private WalletService walletService;
    @Autowired private PaymentFacade paymentFacade;

    private Long scheduleId;
    private Long seatId;

    @BeforeEach
    void setUp() {
        for (int i=0; i<5; i++) {
            UserEntity user = userJpaRepository.save(UserEntity.builder()
                .name("유저" + i) .email("user" + i + "@test.com") .password("1234")
                .build());
            walletJpaRepository.save(
                WalletEntity.builder()
                    .id(user.getId()) .userId(user.getId()) .balance(10000L)
                    .build()
            );
            userIds.add(user.getId());
            queueService.issueToken(user.getId());
        }

        ConcertEntity concert = concertJpaRepository.save(ConcertEntity.builder()
            .title("동시성 테스트 콘서트").description("설명")
            .build());
        ConcertScheduleEntity schedule = concertScheduleJpaRepository.save(
            ConcertScheduleEntity.builder()
                .concertId(concert.getId())
                .date(LocalDateTime.now().plusDays(30))
                .reservationOpenTime(LocalDateTime.now().minusDays(1))
                .reservationCloseTime(LocalDateTime.now().plusDays(29))
                .build()
        );
        SeatEntity seat = seatJpaRepository.save(SeatEntity.builder().seatNo(1).build());

        scheduleId = schedule.getId();
        seatId = seat.getId();
    }

    @AfterEach
    void tearDown() {
        userIds.forEach(queueService::removeFromQueue);
        reservationJpaRepository.deleteAll();
        seatJpaRepository.deleteAll();
        concertScheduleJpaRepository.deleteAll();
        concertJpaRepository.deleteAll();
        walletJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
        userIds.clear();
    }


    // ===
    @Test
    public void 동시_예약시_5명_중_1명만_성공() throws InterruptedException {
        int threadCount = 5;
        // 스레드 5개
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1); // 동시 출발 신호
        CountDownLatch doneLatch = new CountDownLatch(threadCount); // 완료 대기
        // 성공, 실패 카운트
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i=0; i<threadCount; i++) {
            final Long userId = userIds.get(i);
            executorService.submit(() -> {
                try {
                    startLatch.await(); // 출발 대기
                    reservationFacade.holdSeat(scheduleId, userId, seatId);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // 전체 동시 출발
        doneLatch.await(); // 모든 스레드 완료 대기
        executorService.shutdown();

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(4);
    }

    // ===
    @Test
    void 동시_충전시_모든_금액이_반영되어야_한다() throws InterruptedException {
        // given
        Long userId = userIds.get(0);
        // setUp에서 초기 잔액 10000L로 세팅됨
        int threadCount = 10;
        long chargeAmount = 1000L;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1); // 동시 출발 신호
        CountDownLatch doneLatch = new CountDownLatch(threadCount); // 완료 대기
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    walletService.charge(userId, chargeAmount);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    // no-op
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        // then
        WalletEntity finalWallet = walletJpaRepository.findByUserId(userId).get();
        // 10000 + 10회 * 1000 = 20000 이어야 하지만, 락 없이는 Lost Update로 일부 충전이 누락될 수 있음
        assertThat(successCount.get()).isEqualTo(threadCount);
        assertThat(finalWallet.getBalance()).isEqualTo(10000L + (long)(threadCount * chargeAmount));
    }

    // ===
    @Test
    void 동시_결제시_잔액이_초과되면_안된다() throws InterruptedException {
        // given
        Long userId = userIds.get(0);
        // 잔액을 5000으로 재설정 → 두 결제(5000 + 5000 = 10000)가 동시에 오면 1건만 성공해야 함
        WalletEntity wallet = walletJpaRepository.findByUserId(userId).get();
        ReflectionTestUtils.setField(wallet, "balance", 5000L);
        walletJpaRepository.saveAndFlush(wallet);

        // 두 번째 좌석 생성 (같은 유저가 다른 좌석에 각각 예약)
        SeatEntity seat2 = seatJpaRepository.save(SeatEntity.builder().seatNo(2).build());

        // 같은 유저가 두 좌석을 예약 (스레드 시작 전에 생성)
        ReservationResponse reservation1 = reservationFacade.holdSeat(scheduleId, userId, seatId);
        ReservationResponse reservation2 = reservationFacade.holdSeat(scheduleId, userId, seat2.getId());
        Long reservationId1 = reservation1.getReservationId();
        Long reservationId2 = reservation2.getReservationId();

        int threadCount = 2;
        long payAmount = 5000L;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1); // 동시 출발 신호
        CountDownLatch doneLatch = new CountDownLatch(threadCount); // 완료 대기
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // Thread 1: reservation1 결제
        executor.submit(() -> {
            try {
                startLatch.await();
                paymentFacade.processPayment(reservationId1, userId, payAmount);
                successCount.incrementAndGet();
            } catch (Exception e) {
                failCount.incrementAndGet();
            } finally {
                doneLatch.countDown();
            }
        });

        // Thread 2: reservation2 결제
        executor.submit(() -> {
            try {
                startLatch.await();
                paymentFacade.processPayment(reservationId2, userId, payAmount);
                successCount.incrementAndGet();
            } catch (Exception e) {
                failCount.incrementAndGet();
            } finally {
                doneLatch.countDown();
            }
        });

        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        // then
        WalletEntity finalWallet = walletJpaRepository.findByUserId(userId).get();
        // 잔액이 마이너스가 되면 안 됨
        assertThat(finalWallet.getBalance()).isGreaterThanOrEqualTo(0L);
        // 잔액 5000으로는 5000짜리 결제를 1건만 처리 가능
        // 락 없이는 두 스레드 모두 잔액=5000 snapshot을 읽어 둘 다 성공할 수 있음 (Lost Update)
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(1);
    }

}
