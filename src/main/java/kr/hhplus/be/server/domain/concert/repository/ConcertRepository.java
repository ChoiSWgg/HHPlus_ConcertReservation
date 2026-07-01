package kr.hhplus.be.server.domain.concert.repository;

import kr.hhplus.be.server.domain.concert.entity.ConcertEntity;
import java.util.Optional;

public interface ConcertRepository {

    // 콘서트 단건 조회
    Optional<ConcertEntity> findById(Long concertId);
}
