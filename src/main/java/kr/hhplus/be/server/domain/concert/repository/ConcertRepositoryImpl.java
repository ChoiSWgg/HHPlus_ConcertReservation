package kr.hhplus.be.server.domain.concert.repository;

import kr.hhplus.be.server.domain.concert.entity.ConcertEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ConcertRepositoryImpl implements ConcertRepository{

    private final ConcertJpaRepository concertJpaRepository;

    @Override
    public Optional<ConcertEntity> findById(Long concertId) {
        return concertJpaRepository.findById(concertId);
    }
}
