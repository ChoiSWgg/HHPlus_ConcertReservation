package kr.hhplus.be.server.domain.wallet.controller;

import kr.hhplus.be.server.domain.user.dto.ChargeRequest;
import kr.hhplus.be.server.domain.user.dto.ChargeResponse;
import kr.hhplus.be.server.domain.user.dto.PointResponse;
import kr.hhplus.be.server.domain.wallet.service.WalletService;
import kr.hhplus.be.server.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/{userId}/points")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping
    public ResponseEntity<ApiResponse<PointResponse>> getBalance(
        @PathVariable Long userId
    ) {
        return ResponseEntity.ok(ApiResponse.success(walletService.getBalance(userId)));
    }

    @PatchMapping
    public ResponseEntity<ApiResponse<ChargeResponse>> chargePoint(
        @PathVariable Long userId,
        @RequestBody ChargeRequest request
    ) {
        return ResponseEntity.ok(
            ApiResponse.success(walletService.charge(userId, request.getAmount()))
        );
    }
}
