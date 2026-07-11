package kr.hhplus.be.server.domain.queue.service;

import kr.hhplus.be.server.domain.queue.dto.QueueStatusResponse;
import kr.hhplus.be.server.domain.queue.repository.QueueRepository;
import kr.hhplus.be.server.domain.user.entity.UserEntity;
import kr.hhplus.be.server.domain.user.repository.UserRepository;
import kr.hhplus.be.server.global.exception.CustomException;
import kr.hhplus.be.server.global.exception.ErrorCode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class QueueServiceTest {

    @Mock UserRepository userRepository;
    @Mock QueueRepository queueRepository;
    @InjectMocks QueueService queueService;

    @Nested
    class issueToken {

        @Test
        void 유저가_없으면_USER_NOT_FOUND_예외() {
            given(userRepository.findById(1L)).willReturn(Optional.empty());

            assertThatThrownBy(()-> queueService.issueToken(1L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
        }

        @Test
        void 이미_대기열에_있으면_ALREADY_IN_QUEUE_예외 () {
            given(userRepository.findById(1L)).willReturn(Optional.of(mock(UserEntity.class)));
            given(queueRepository.isUserInQueue(1L)).willReturn(true);

            assertThatThrownBy(() -> queueService.issueToken(1L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ALREADY_IN_QUEUE);
        }

        @Test
        void rank가_100미만이면_ACTIVE_상태로_반환() {
            given(userRepository.findById(1L)).willReturn(Optional.of(mock(UserEntity.class)));
            given(queueRepository.isUserInQueue(1L)).willReturn(false);
            given(queueRepository.addToQueue(1L)).willReturn(0L);

            QueueStatusResponse result = queueService.issueToken(1L);

            assertThat(result.getStatus()).isEqualTo("ACTIVE");
            assertThat(result.getWaitingOrder()).isEqualTo(1L);
        }

        @Test
        void rank가_100이상이면_WAIT_상태로_반환() {
            given(userRepository.findById(1L)).willReturn(Optional.of(mock(UserEntity.class)));
            given(queueRepository.isUserInQueue(1L)).willReturn(false);
            given(queueRepository.addToQueue(1L)).willReturn(100L);

            QueueStatusResponse result = queueService.issueToken(1L);

            assertThat(result.getStatus()).isEqualTo("WAIT");
        }

    }

    @Nested
    class getQueueStatus {

        @Test
        void 대기열에_없으면_USER_NOT_IN_QUEUE_예외() {
            given(queueRepository.isUserInQueue(1L)).willReturn(false);

            assertThatThrownBy(() -> queueService.getQueueStatus(1L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.USER_NOT_IN_QUEUE);
        }

        @Test
        void 정상_조회시_QueueStatusResponse_반환() {
            given(queueRepository.isUserInQueue(1L)).willReturn(true);
            given(queueRepository.getRank(1L)).willReturn(5L);
            given(queueRepository.getToken(1L)).willReturn("token-uuid");

            QueueStatusResponse result = queueService.getQueueStatus(1L);

            assertThat(result.getStatus()).isEqualTo("ACTIVE");
            assertThat(result.getWaitingOrder()).isEqualTo(6L);
            assertThat(result.getToken()).isEqualTo("token-uuid");
        }
    }
}