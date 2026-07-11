package kr.hhplus.be.server.domain.concert.repository;

import kr.hhplus.be.server.domain.concert.entity.SeatEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SeatRepositoryImpl implements SeatRepository{

    private final SeatJpaRepository seatJpaRepository;

    @Override
    public List<SeatEntity> findAll() { return seatJpaRepository.findAll(); }
}
