# 콘서트 예약 서비스 (Concert Reservation Service)

## 📌 아키텍처 문서 목록
- [API 명세서](./api-specification.md)
- [데이터 모델링 (ERD)](./erd.md)
- [인프라 구조 설계](./infra_structure.md)
- [의사결정 기록 (ADR 목록)](./adr/README.md)

## 🏛️ 주요 의사결정 (ADR 요약)
1. **[ADR-001] 대기열 관리 방식 결정**: Redis Sorted Set vs RDBMS
    - *결정*: 동시성과 빠른 조회를 위해 Redis Sorted Set을 대기열 토큰 관리에 사용하기로 함.
2. **[ADR-002] 좌석 임시 배정(Held) 제어 방식**: Redis TTL 기반 관리
    - *결정*: 5분의 임시 제한시간 보장과 DB 부하 경감을 위해 Redis 분산 락 및 Redis TTL 만료 이벤트를 활용하기로 함.

---
#### Running Docker Containers

`local` profile 로 실행하기 위하여 인프라가 설정되어 있는 Docker 컨테이너를 실행해주셔야 합니다.

```bash
docker-compose up -d
```