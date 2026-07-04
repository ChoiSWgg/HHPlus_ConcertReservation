package kr.hhplus.be.server.domain.wallet.service;

import kr.hhplus.be.server.domain.user.dto.ChargeResponse;
import kr.hhplus.be.server.domain.user.dto.PointResponse;
import kr.hhplus.be.server.domain.user.entity.UserEntity;
import kr.hhplus.be.server.domain.user.repository.UserRepository;
import kr.hhplus.be.server.domain.wallet.entity.WalletEntity;
import kr.hhplus.be.server.domain.wallet.repository.WalletRepository;
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
class WalletServiceTest {

    @Mock UserRepository userRepository;
    @Mock WalletRepository walletRepository;
    @InjectMocks WalletService walletService;

    @Nested
    class getBalance {

        @Test
        void 유저가_없으면_USER_NOT_FOUND_예외() {
            given(userRepository.findById(1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> walletService.getBalance(1L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.USER_NOT_FOUND);
        }

        @Test
        void 유저가_있지만_지갑이_없으면_NOT_FOUND_예외() {
            given(userRepository.findById(1L)).willReturn(Optional.of(mock(UserEntity.class)));
            given(walletRepository.findByUserId(1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> walletService.getBalance(1L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.NOT_FOUND);
        }

        @Test
        void 정상_조회시_PointResponse_반환() {
            WalletEntity wallet = mock(WalletEntity.class);
            given(userRepository.findById(1L)).willReturn(Optional.of(mock(UserEntity.class)));
            given(walletRepository.findByUserId(1L)).willReturn(Optional.of(wallet));
            given(wallet.getBalance()).willReturn(5000L);

            PointResponse result = walletService.getBalance(1L);

            assertThat(result.getPoints()).isEqualTo(5000L);
        }
    }

    @Nested
    class charge {

        @Test
        void 충전금액이_0이하면_INVALID_AMOUNT_예외() {
            assertThatThrownBy(() -> walletService.charge(1L, 0L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_AMOUNT);
        }

        @Test
        void 유저가_없으면_USER_NOT_FOUND_예외() {
            given(userRepository.findById(1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> walletService.charge(1L, 1000L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.USER_NOT_FOUND);
        }

        @Test
        void 정상_충전시_ChargeResponse_반환() {
            WalletEntity wallet = mock(WalletEntity.class);
            given(userRepository.findById(1L)).willReturn(Optional.of(mock(UserEntity.class)));
            given(walletRepository.findByUserId(1L)).willReturn(Optional.of(wallet));
            given(wallet.getBalance()).willReturn(6000L);
            given(walletRepository.save(wallet)).willReturn(wallet);

            ChargeResponse result = walletService.charge(1L, 1000L);

            assertThat(result.getChargedAmount()).isEqualTo(1000L);
            assertThat(result.getTotalPoints()).isEqualTo(6000L);
        }
    }
}