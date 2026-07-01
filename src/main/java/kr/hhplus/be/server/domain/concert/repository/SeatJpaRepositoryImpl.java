package kr.hhplus.be.server.domain.concert.repository;

import kr.hhplus.be.server.domain.concert.entity.ConcertEntity;
import kr.hhplus.be.server.domain.concert.entity.ConcertScheduleEntity;
import kr.hhplus.be.server.domain.concert.entity.SeatEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SeatJpaRepositoryImpl implements ConcertRepository{

    private final ConcertJpaRepository concertJpaRepository;
    private final ConcertScheduleJpaRepository concertScheduleJpaRepository;
    private final SeatJpaRepository seatJpaRepository;

    @Override
    public Optional<ConcertEntity> findById(Long concertId) {
        return concertJpaRepository.findById(concertId);
    }

    @Override
    public List<ConcertScheduleEntity> findScheduleByConcertId(Long concertId) {
        return concertScheduleJpaRepository.findByConcertId(concertId);
    }

    @Override
    public Optional<ConcertScheduleEntity> findScheduleById(Long scheduleId) {
        return concertScheduleJpaRepository.findById(scheduleId);
    }

    @Override
    public List<SeatEntity> findAllSeats() {
        return seatJpaRepository.findAll();
    }
}
