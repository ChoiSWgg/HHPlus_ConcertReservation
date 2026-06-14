package kr.hhplus.be.server;

import jakarta.annotation.PreDestroy;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

// Testcontainers를 이용해 통합 테스트용 MySQL 컨테이너를 자동으로 띄우고 데이터베이스 연결 설정을 동적으로 주입하는 설정 클래스
@Configuration
class TestcontainersConfiguration {

	// 테스트용 MySQL 독커 컨테이너 정의 (싱글톤 패턴으로 공유하기 위해 static 선언)
	public static final MySQLContainer<?> MYSQL_CONTAINER;

	static {
		// 1. MySQL 8.0 버전 이미지 기반으로 DB 이름, 계정, 비밀번호 설정 및 컨테이너 실행
		MYSQL_CONTAINER = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
			.withDatabaseName("hhplus")
			.withUsername("test")
			.withPassword("test");
		MYSQL_CONTAINER.start();

		// 2. 실행된 컨테이너의 동적 포트/접속 정보를 스프링 데이터소스 환경 변수에 주입
		System.setProperty("spring.datasource.url", MYSQL_CONTAINER.getJdbcUrl() + "?characterEncoding=UTF-8&serverTimezone=UTC");
		System.setProperty("spring.datasource.username", MYSQL_CONTAINER.getUsername());
		System.setProperty("spring.datasource.password", MYSQL_CONTAINER.getPassword());
	}

	// 3. 스프링 애플리케이션(테스트) 컨텍스트가 종료될 때 실행 중인 DB 컨테이너를 안전하게 자원 해제
	@PreDestroy
	public void preDestroy() {
		if (MYSQL_CONTAINER.isRunning()) {
			MYSQL_CONTAINER.stop();
		}
	}
}