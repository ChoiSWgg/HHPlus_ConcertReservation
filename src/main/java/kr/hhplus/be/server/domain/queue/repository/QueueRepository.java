package kr.hhplus.be.server.domain.queue.repository;

// 대기열(queue)는 Redis 기반이므로, Redis 연산을 추상화한 메서드들을 정의한다.
public interface QueueRepository {

    // 유저가 이미 대기열에 있는지 확인
    boolean isUserInQueue(Long userId);

    // 대기열에 추가하고 현재 0-based rank를 반환
    long addToQueue(Long userId);

    // 현재 0-based rank 조회 (대기열에 없으면 null)
    Long getRank(Long userId);

    // 유저의 토큰 문자열 조회
    String getToken(Long userId);

    // 토큰 저장
    void storeToken(Long userId, String token);

    // 대기열에서 제거
    void removeFromQueue(Long userId);
}
