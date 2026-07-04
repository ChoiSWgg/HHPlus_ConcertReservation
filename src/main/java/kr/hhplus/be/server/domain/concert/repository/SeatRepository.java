package kr.hhplus.be.server.domain.concert.repository;

import kr.hhplus.be.server.domain.concert.entity.SeatEntity;

import java.util.List;

public interface SeatRepository {

    // 전체 좌석 목록 조회 (1~50명)
    List<SeatEntity> findAll();

}
