# HHPlus Concert Reservation - 비즈니스 로직 구현 계획

## Context
섹션3 과제: 콘서트 예약 서비스의 6개 시나리오에 대한 비즈니스 로직 구현 및 단위 테스트 작성.
- **Clean Architecture**: Reservation(좌석 임시 배정) + Payment(결제)
- **Layered Architecture**: Concert(조회), Queue(대기열), User/Wallet(포인트)
- 단위 테스트: Mockito만 사용, 외부 의존성 전부 Mock

---

## 아키텍처 구조

```
Clean Arch (reservation, payment): 
  interfaces/web → application(UseCase) → domain/model + domain/repository ← infrastructure/persistence

Layered (concert, queue, user, wallet):
  controller → service → repository(interface) → JpaRepository / RedisRepository → entity
```

---

## Phase 1: 도메인 모델 보강 (기존 파일 수정)

### Reservation.java
- 기존 String 필드만 있는 POJO → 완전한 도메인 모델로 교체
- 추가: `static Reservation.hold(userId, scheduleId, seatId)` → status="HELD", reservedAt=now, expiredAt=now+5분
- 추가: `static Reservation.reconstruct(...)` → 엔티티→도메인 역매핑용
- 추가: `boolean isExpired()`, `boolean isHeld()`, `boolean isConfirmed()`, `void confirm()`

### Payment.java
- 필드 타입 정비 (price: Integer → Long)
- 추가: `static Payment.of(reservationId, price)` → paidAt=now

### WalletEntity.java
- 추가: `void charge(Long amount)`, `void deduct(Long amount)`

### ReservationEntity.java / PaymentEntity.java
- 추가: `static from(도메인 모델)`, `도메인모델 toDomain()` 변환 메서드
- PaymentEntity.price: Integer → Long

---

## Phase 2: Repository 인터페이스 정의 (기존 파일 수정)

| 파일 | 추가 메서드 |
|------|------------|
| `ConcertRepository` | `findById`, `findSchedulesByConcertId`, `findScheduleById`, `findAllSeats` |
| `ReservationRepository` | `findById(Long)`, `findAllByScheduleId(Long)` (기존 2개 유지) |
| `PaymentRepository` | `save(Payment)` |
| `WalletRepository` | `findByUserId(Long)`, `save(WalletEntity)` |
| `UserRepository` | `findById(Long)` |
| `QueueRepository` | `isUserInQueue`, `addToQueue`, `getRank`, `getToken`, `storeToken` |

---

## Phase 3: 신규 생성 파일 목록

### Concert 도메인 (Layered)
```
domain/concert/repository/
  SpringConcertJpa.java           (extends JpaRepository<ConcertEntity, Long>)
  SpringScheduleJpa.java          (+ findByConcertId)
  SpringSeatJpa.java              (extends JpaRepository<SeatEntity, Long>)
  ConcertJpaRepository.java       (implements ConcertRepository, 3개 Spring JPA 위임)

domain/concert/dto/
  ConcertScheduleResponse.java
  SeatStatusResponse.java
```

### Queue 도메인 (Layered - Redis)
```
domain/queue/repository/QueueRedisRepository.java
  Redis 구조:
    queue:waiting → ZSET (member=userId, score=timestamp)
    queue:token:{userId} → STRING (UUID)
  Active 기준: rank < 100

domain/queue/dto/
  QueueTokenRequest.java
  QueueTokenResponse.java
```

### User / Wallet 도메인 (Layered)
```
domain/user/repository/SpringUserJpa.java
domain/user/repository/UserJpaRepository.java
domain/user/dto/PointResponse.java, ChargeRequest.java, ChargeResponse.java

domain/wallet/repository/SpringWalletJpa.java     (+ findByUserId)
domain/wallet/repository/WalletJpaRepository.java
```

### Reservation / Payment (Clean Arch - 기존 파일 보완)
```
SpringReservationJpa.java: + findByScheduleIdAndSeatId, findByScheduleId 추가
ReservationJpaRepository.java: 4개 메서드 완전 구현
PaymentJpaRepository.java: save 구현

domain/reservation/interfaces/web/dto/ReservationRequest.java, ReservationResponse.java
domain/payment/interfaces/web/dto/PaymentRequest.java, PaymentResponse.java
```

---

## Phase 4: 서비스 비즈니스 로직

### WalletService (Layered)
의존: `UserRepository`, `WalletRepository`
```
getBalance(userId): 유저 존재 확인 → 지갑 조회 → PointResponse 반환
charge(userId, amount):
  - amount <= 0 → INVALID_AMOUNT
  - 유저 미존재 → USER_NOT_FOUND
  - 지갑 charge() → save → ChargeResponse 반환
```

### ConcertService (Layered)
의존: `ConcertRepository`, `ReservationRepository`(cross-domain, 인터페이스만)
```
getSchedules(concertId): 콘서트 미존재 → CONCERT_NOT_FOUND, 스케줄 목록 반환
getSeats(scheduleId): 스케줄 미존재 → CONCERT_SCHEDULE_NOT_FOUND
  → 전체 좌석(1~50) + 해당 스케줄 예약 목록 조합
  → 상태 판정: CONFIRMED→sold, HELD+미만료→held, 나머지→available
```

### QueueService (Layered)
의존: `UserRepository`, `QueueRepository`
```
issueToken(userId):
  - 유저 미존재 → USER_NOT_FOUND
  - 이미 대기 중 → ALREADY_IN_QUEUE
  - UUID 생성 → ZSET 추가 → rank 기반 WAIT/ACTIVE 판정

getQueueStatus(userId):
  - 대기열 미등록 → USER_NOT_IN_QUEUE
  - rank 조회 → WAIT/ACTIVE 반환
```

### ReservationService (Clean Arch)
의존: `ReservationRepository`
```
holdSeat(scheduleId, userId, seatId):
  - 기존 예약 조회:
    · CONFIRMED → SEAT_NOT_AVAILABLE
    · HELD + 미만료 → SEAT_ALREADY_HELD
    · HELD + 만료 → 통과 (새 예약 허용)
  - Reservation.hold() → save → ReservationResponse 반환
```

### PaymentService (Clean Arch) - @Transactional
의존: `ReservationRepository`, `WalletRepository`, `PaymentRepository`
```
processPayment(reservationId, amount):
  1. 예약 미존재 → NOT_FOUND
  2. isExpired() → RESERVATION_EXPIRED (410)
  3. 지갑 미존재 → NOT_FOUND
  4. balance < amount → INSUFFICIENT_POINTS
  5. wallet.deduct() → walletRepo.save()
  6. Payment.of() → paymentRepo.save()
  7. reservation.confirm() → reservationRepo.save()
  8. PaymentResponse(paymentId, amount, "CONFIRMED") 반환
```

---

## Phase 5: Controller 구현

| Controller | 메서드 | 반환 코드 |
|-----------|--------|---------|
| ConcertController | GET /concerts/{id}/schedules | 200 |
| ConcertController | GET /schedules/{id}/seats | 200 |
| ReservationController | POST /schedules/{id}/reservations | 201 |
| PaymentController | POST /reservations/{id}/payments | 201 |
| UserController | GET /users/{id}/points | 200 |
| UserController | PATCH /users/{id}/points | 200 |
| QueueController | POST /queues | 201 |
| QueueController | GET /queues/{userId} | 200 |

---

## Phase 6: 보안 설정 수정

- `JwtAuthenticationFilter.doFilterInternal`: `filterChain.doFilter()` 호출 누락 버그 수정
- `SecurityConfig`: 현재는 `anyRequest().permitAll()` 로 단순화 (비즈니스 로직 테스트 집중)

---

## Phase 7: 단위 테스트 (Mockito, @ExtendWith(MockitoExtension.class))

```
test/.../domain/
  concert/service/ConcertServiceTest.java
  queue/service/QueueServiceTest.java
  wallet/service/WalletServiceTest.java
  reservation/application/ReservationServiceTest.java
  payment/application/PaymentServiceTest.java
```

### 테스트 케이스 목록

**ConcertServiceTest** (Mock: ConcertRepository, ReservationRepository)
- getSchedules: 정상 / 콘서트 미존재
- getSeats: 정상(available) / held 상태 / sold 상태 / 만료 HELD→available / 스케줄 미존재

**QueueServiceTest** (Mock: UserRepository, QueueRepository)
- issueToken: 정상(WAIT) / 정상(ACTIVE, rank<100) / 유저 미존재 / 이미 대기 중
- getQueueStatus: ACTIVE / WAIT / 대기열 미등록

**WalletServiceTest** (Mock: UserRepository, WalletRepository)
- getBalance: 정상 / 유저 미존재
- charge: 정상 / amount=0 → INVALID_AMOUNT / amount<0 / 유저 미존재

**ReservationServiceTest** (Mock: ReservationRepository)
- holdSeat: 정상(빈 좌석) / 만료 HELD → 정상 / 활성 HELD → SEAT_ALREADY_HELD / CONFIRMED → SEAT_NOT_AVAILABLE

**PaymentServiceTest** (Mock: ReservationRepository, WalletRepository, PaymentRepository)
- processPayment: 정상 / 예약 미존재 / 만료 → RESERVATION_EXPIRED / 포인트 부족 / 지갑 미존재

---

## 구현 순서
1. 도메인 모델 보강 (Reservation, Payment, WalletEntity)
2. Repository 인터페이스 + JPA 엔티티 변환 메서드
3. Spring JPA / Redis Repository 구현체
4. DTO 클래스
5. Service 비즈니스 로직
6. Controller
7. SecurityConfig 수정
8. 단위 테스트

## 검증 방법
- `./gradlew test` 로 단위 테스트 전체 실행 (Testcontainers 없이 순수 Mockito)
- 각 서비스 테스트에서 성공 케이스 + 에러 케이스 전부 Green 확인
