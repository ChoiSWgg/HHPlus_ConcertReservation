package kr.hhplus.be.server.domain.queue.service;

import kr.hhplus.be.server.domain.queue.dto.QueueStatusResponse;
import kr.hhplus.be.server.domain.queue.repository.QueueRepository;
import kr.hhplus.be.server.domain.user.repository.UserRepository;
import kr.hhplus.be.server.global.exception.CustomException;
import kr.hhplus.be.server.global.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class QueueService {

    private static final long ACTIVE_THRESHOLD = 100L;

    private final UserRepository userRepository;
    private final QueueRepository queueRepository;

    public QueueService(UserRepository userRepository, QueueRepository queueRepository) {
        this.userRepository = userRepository;
        this.queueRepository = queueRepository;
    }

    /**
     * [POST /queues] 대기열 토큰 발급
     * <p>
     * 1. userRepository.findById(userId) 로 유저 존재 확인
     * - 없으면 CustomException(USER_NOT_FOUND) 던지기
     * 2. queueRepository.isUserInQueue(userId) 로 이미 대기열 등록 여부 확인
     * - 이미 있으면 CustomException(ALREADY_IN_QUEUE) 던지기
     * 3. UUID.randomUUID().toString() 으로 토큰 생성
     * 4. queueRepository.storeToken(userId, token) 으로 토큰 저장
     * 5. queueRepository.addToQueue(userId) 로 ZSET에 추가, 0-based rank 반환
     * 6. rank < ACTIVE_THRESHOLD → status = "ACTIVE", 아니면 "WAIT"
     * 7. QueueStatusResponse(userId, token, status, rank + 1) 반환
     */
    public QueueStatusResponse issueToken(Long userId) {
        userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        if (queueRepository.isUserInQueue(userId))
            throw new CustomException(ErrorCode.ALREADY_IN_QUEUE);

        String token = UUID.randomUUID().toString();
        Long rank = queueRepository.addToQueue(userId);
        queueRepository.storeToken(userId, token);
        String status = rank < ACTIVE_THRESHOLD ? "ACTIVE" : "WAIT";
        return new QueueStatusResponse(userId, token, status, rank + 1);
    }

    /**
     * [GET /queues/{userId}] 대기번호 조회
     * <p>
     * 1. queueRepository.isUserInQueue(userId) 로 대기열 등록 여부 확인
     * - 없으면 CustomException(USER_NOT_IN_QUEUE) 던지기
     * 2. queueRepository.getRank(userId) 로 현재 0-based rank 조회
     * 3. queueRepository.getToken(userId) 로 토큰 조회
     * 4. rank < ACTIVE_THRESHOLD → status = "ACTIVE", 아니면 "WAIT"
     * 5. QueueStatusResponse(userId, token, status, rank + 1) 반환
     */
    public QueueStatusResponse getQueueStatus(Long userId) {
        if (!queueRepository.isUserInQueue(userId))
            throw new CustomException(ErrorCode.USER_NOT_IN_QUEUE);
        Long rank = queueRepository.getRank(userId);
        if (rank == null) throw new CustomException(ErrorCode.USER_NOT_IN_QUEUE);
        String token = queueRepository.getToken(userId);
        String status = rank < ACTIVE_THRESHOLD ? "ACTIVE" : "WAIT";
        return new QueueStatusResponse(userId, token, status, rank+1); // 1-based rank
    }

    public void removeFromQueue(Long userId) {
        queueRepository.removeFromQueue(userId);
    }
}
