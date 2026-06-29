package kr.hhplus.be.server.domain.concert.repository;

import kr.hhplus.be.server.domain.concert.entity.ConcertEntity;
import kr.hhplus.be.server.domain.concert.entity.ConcertScheduleEntity;
import kr.hhplus.be.server.domain.concert.entity.SeatEntity;

import java.util.List;
import java.util.Optional;

public interface ConcertRepository {

    // 콘서트 단건 조회
    Optional<ConcertEntity> findById(Long concertId);

    // 콘서트의 전체 회차 목록 조회
    List<ConcertScheduleEntity> findScheduleByConcertId(Long concertId);

    // 특정 회차 단건 조회 (seats 조회 전 존재 확인용)
    Optional<ConcertScheduleEntity> findScheduleById(Long scheduleId);

    // 전체 좌석 목록 조회 (1~50명)
    List<SeatEntity> findAllSeats();

}
