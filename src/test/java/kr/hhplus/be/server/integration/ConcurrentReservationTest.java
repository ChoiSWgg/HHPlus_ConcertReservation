package kr.hhplus.be.server.integration;

import kr.hhplus.be.server.domain.concert.entity.ConcertEntity;
import kr.hhplus.be.server.domain.concert.entity.ConcertScheduleEntity;
import kr.hhplus.be.server.domain.concert.entity.SeatEntity;
import kr.hhplus.be.server.domain.concert.repository.ConcertJpaRepository;
import kr.hhplus.be.server.domain.concert.repository.ConcertScheduleJpaRepository;
import kr.hhplus.be.server.domain.concert.repository.SeatJpaRepository;
import kr.hhplus.be.server.domain.queue.service.QueueService;
import kr.hhplus.be.server.domain.reservation.application.ReservationFacade;
import kr.hhplus.be.server.domain.reservation.infrastructure.persistence.ReservationJpaRepository;
import kr.hhplus.be.server.domain.user.entity.UserEntity;
import kr.hhplus.be.server.domain.user.repository.UserJpaRepository;
import kr.hhplus.be.server.domain.wallet.entity.WalletEntity;
import kr.hhplus.be.server.domain.wallet.repository.WalletJpaRepository;
import kr.hhplus.be.server.support.AbstractIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

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

}
