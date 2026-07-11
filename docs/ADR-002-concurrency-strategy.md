> 📌 작성 시점: 항해99 HH Plus 4주차 (Section 4)

# ADR-002: 동시성 제어 전략 — DB UNIQUE 제약 + rehold 패턴

## 상태
결정됨

## 배경

여러 유저가 동시에 같은 좌석을 예약할 때 반드시 한 명만 성공해야 한다.
동시성 제어 방식으로 아래 세 가지를 검토했다.

## 선택지 비교

| 방식 | 동작 방식 | 장점 | 단점 |
|------|----------|------|------|
| 낙관적 락 (`@Version`) | DB 쓰기 시 충돌 감지 후 재시도 | 경쟁이 적을 때 빠름 | 경쟁이 많으면 재시도가 폭발적으로 늘어남 |
| 비관적 락 (`SELECT FOR UPDATE`) | DB 읽기 시점에 다른 사람 접근 차단 | 충돌이 많아도 안전 | 기다리는 시간 발생, 교착 상태 위험 |
| DB UNIQUE 제약 | DB 쓰기 시 중복이면 DB가 자동 거부 | 별도 락 코드 없이 단순하게 보장 | 만료된 행 재삽입 시 DB 에러 발생 |

## 결정

**DB UNIQUE 제약 (`schedule_id`, `seat_id`) + `rehold` 패턴** 을 사용한다.

```sql
UNIQUE KEY reservations_unique_schedule_seat (schedule_id, seat_id)
```

같은 스케줄 + 좌석 조합은 DB에 딱 한 행만 존재할 수 있다.
동시에 여러 명이 DB에 쓰려 하면 첫 번째만 성공하고 나머지는 DB가 거부한다.

### rehold 패턴이 필요한 이유

만료된 예약이 이미 DB에 있는 상태에서 새 유저가 같은 좌석을 예약하면,
새로 DB에 쓰려 해도 UNIQUE 제약 때문에 거부된다.

이를 해결하기 위해 새 행을 추가하는 대신 **기존 행을 덮어쓰는** `rehold()` 를 사용한다.

```java
// id를 세팅하면 JPA가 새 행 추가(INSERT) 대신 기존 행 수정(UPDATE) 수행
public static Reservation rehold(Long id, Long userId, Long scheduleId, Long seatId) {
    Reservation reservation = hold(userId, scheduleId, seatId);
    reservation.id = id;
    return reservation;
}
```

## 이유

콘서트 예약처럼 **같은 좌석에 동시 요청이 몰리는** 상황은 경쟁이 매우 많다.
낙관적 락은 경쟁이 많을수록 재시도가 늘어나 오히려 성능이 나빠진다.

DB UNIQUE 제약은 DB 레벨에서 중복 쓰기를 막아주므로,
별도 락 코드 없이 "한 명만 성공" 을 보장할 수 있다.

비관적 락도 유효하지만, 지금 구조에서는 UNIQUE 제약만으로 요구사항을 충족하므로 불필요한 복잡도를 피했다.

## 결과

- `reservations` 테이블에 `UNIQUE(schedule_id, seat_id)` 제약 적용
- 동시 요청 시 첫 번째 DB 쓰기만 성공, 나머지는 `SEAT_ALREADY_HELD` 응답 반환
- 만료 후 재예약은 `rehold()` 로 기존 행 수정(UPDATE) 처리하여 UNIQUE 위반 없이 동작
