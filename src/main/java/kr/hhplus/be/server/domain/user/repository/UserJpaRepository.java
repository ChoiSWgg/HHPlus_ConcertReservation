package kr.hhplus.be.server.domain.user.repository;

import kr.hhplus.be.server.domain.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {
}
