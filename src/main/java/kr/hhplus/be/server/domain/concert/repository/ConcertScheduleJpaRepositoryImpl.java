package kr.hhplus.be.server.domain.concert.repository;

import kr.hhplus.be.server.domain.concert.entity.ConcertScheduleEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ConcertScheduleJpaRepositoryImpl implements ConcertScheduleRepository{

    private final ConcertScheduleJpaRepository concertScheduleJpaRepository;

    @Override
    public List<ConcertScheduleEntity> findByConcertId(Long concertId) {
        return concertScheduleJpaRepository.findByConcertId(concertId);
    }

    @Override
    public Optional<ConcertScheduleEntity> findById(Long scheduleId) {
        return concertScheduleJpaRepository.findById(scheduleId);
    }
}
