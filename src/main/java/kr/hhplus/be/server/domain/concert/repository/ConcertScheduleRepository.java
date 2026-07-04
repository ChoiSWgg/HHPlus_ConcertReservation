package kr.hhplus.be.server.domain.concert.repository;

import kr.hhplus.be.server.domain.concert.entity.ConcertScheduleEntity;

import java.util.List;
import java.util.Optional;

public interface ConcertScheduleRepository {
    // 콘서트의 전체 회차 목록 조회
    List<ConcertScheduleEntity> findByConcertId(Long concertId);

    // 특정 회차 단건 조회 (seats 조회 전 존재 확인용)
    Optional<ConcertScheduleEntity> findById(Long scheduleId);
}
