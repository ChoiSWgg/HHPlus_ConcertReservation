package kr.hhplus.be.server.domain.user.repository;

import kr.hhplus.be.server.domain.user.entity.UserEntity;

import java.util.Optional;

public interface UserRepository {

    // 유저 단건 조회
    Optional<UserEntity> findById(Long userId);
}
