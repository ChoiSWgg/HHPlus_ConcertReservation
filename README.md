# 콘서트 예약 서비스 (Concert Reservation Service)

## 📌 아키텍처 문서 목록
- [API 명세서](./docs/api-specification.md)
- [데이터 모델링 (ERD)](./docs/erd.md)
- [인프라 구조 설계](./docs/infra_structure.md)

## 🏛️ 주요 의사결정 (ADR 요약)
1. **[ADR-001] 대기열 관리 방식 결정**: Redis Sorted Set vs RDBMS
    - *결정*: 대규모 트래픽 시 RDBMS 병목을 방지하고 빠른 순번 조회를 보장하기 위해, 대기열 토큰 관리를 Redis Sorted Set(ZSET) 자료구조를 사용.


2. **[ADR-002] 좌석 임시 배정(Held) 제어 방식**: Redis TTL 기반 관리
    - *결정*: 5분간의 좌석 선점 시간 보장 및 중복 예약 방지를 위해 Redis 분산 락과 DB 수준의 복합 UNIQUE INDEX를 결합.


3. **[ADR-003] 포인트 연산 레이스 컨디션 방어**
   - *결정*:충전 및 차감 연산 시 무결성을 확보하기 위해 DB 레벨에서 비관적 락(SELECT ... FOR UPDATE)을 활용합니다.

---
#### Running Docker Containers

`local` profile 로 실행하기 위하여 인프라가 설정되어 있는 Docker 컨테이너를 실행해주셔야 합니다.

```bash
docker-compose up -d
```

---

## 프로젝트 구조
```text
kr.hhplus.be.server
├── interfaces/                     # 1. [Inbound Adapter] 외부 요청을 처리하는 최외곽 껍데기
│   ├── web/                        # HTTP REST API 컨트롤러 및 요청/응답 DTO 관리
│   │   ├── concert/                # 콘서트 마스터, 회차, 좌석 조회 API
│   │   ├── reservation/            # 좌석 임시 배정(선점) 요청 API
│   │   ├── queue/                  # 대기열 토큰 발급 및 대기 순번 조회 API
│   │   ├── payment/                # 결제 요청 및 검증 API
│   │   └── user/                   # 포인트 충전, 잔액 조회 API
│   └── support/                    # 글로벌 인터셉터(ACTIVE 토큰 검증), 전역 예외 핸들러
│
├── application/                    # 1. [Inbound Port / Usecase] 비즈니스 흐름 조율 레이어 (Facade)
│   ├── ConcertApplicationFacade.java  # 대기열 유효성 검증 후 좌석 선점을 연계하는 유스케이스 조율
│   ├── QueueFacade.java            # 대기열 진입 및 실시간 순번 파악 흐름 조율
│   └── UserPointFacade.java        # 포인트 충전/차감 및 트랜잭션 범위 제어
│
├── domain/                         # 2. [Core Domain] 외부 기술로부터 완벽히 격리된 비즈니스 심장
│   ├── concert/
│   │   ├── model/                  # Concert, ConcertSchedule, Seat 등 순수 도메인 모델 (비즈니스 룰)
│   │   ├── repository/             # ConcertRepository - [Outbound Port] 인프라팀에 제시하는 추상 규격
│   │   └── service/                # ConcertService - 콘서트/좌석 관련 순수 비즈니스 로직 구현체
│   ├── reservation/
│   │   ├── model/                  # Reservation (임시 선점 만료 여부, 상태 전이 규칙 내포)
│   │   ├── repository/             # ReservationRepository - [Outbound Port]
│   │   └── service/                # ReservationService - 예약 상태 변경 및 만료 처리 정책
│   ├── queue/
│   │   ├── model/                  # QueueToken (대기열 토큰 도메인 모델)
│   │   ├── repository/             # QueueRepository - [Outbound Port] 대기열 저장소 규격 인터페이스
│   │   └── service/                # QueueService - 대기 번호 연산 및 토큰 활성화 비즈니스 로직
│   ├── payment/
│   │   └── ...                     # Payment 도메인 모델, Outbound Port, 순수 결제 서비스
│   └── user/
│       └── ...                     # User, Wallet 도메인 모델, Outbound Port, 포인트 연산 서비스
│
└── infrastructure/                 # 3. [Outbound Adapter] 외부 기술 및 인프라와 연결되는 실제 플러그
    ├── persistence/                # RDB (MySQL / Spring Data JPA) 관련 구현체
    │   ├── concert/                # ConcertEntity, ConcertScheduleEntity, ConcertJpaRepositoryImpl
    │   ├── reservation/            # ReservationEntity, ReservationJpaRepositoryImpl
    │   ├── user/                   # WalletEntity, WalletJpaRepositoryImpl (DB 비관적 락 조회 구현)
    │   └── spring/                 # Spring Data JPA 인터페이스 모음 (JpaRepository 상속 클래스)
    ├── redis/                      # Redis 기반의 인프라 기술 구현체
    │   ├── queue/                  # QueueRedis
```