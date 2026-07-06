package kr.hhplus.be.server.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/*
    모든 통합 테스트 클래스마다 도커 컨테이너 설정 코드를 중복해서 짜면 테스트 속도가 느려집니다.
    하나의 컨테이너를 공유해서 쓸 수 있도록 상위 베이스 클래스를 만듭니다.
 */
@SpringBootTest
@Testcontainers
public abstract class AbstractIntegrationTest {


    @Container
    static final MySQLContainer<?>mysql = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("hhplus")
        .withUsername("application")
        .withPassword("application");

    @Container
    static final GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
        .withExposedPorts(6379);

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        // TestContainer가 띄운 MySQL / Redis 주소로 덮어쓰기
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);

        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));

        // 테스트 실행마다 스키마 자동 생성/삭제
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }
}
