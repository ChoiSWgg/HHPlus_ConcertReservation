package kr.hhplus.be.server.domain.payment.interfaces.web;

import kr.hhplus.be.server.domain.payment.application.PaymentFacade;
import kr.hhplus.be.server.domain.payment.interfaces.web.dto.PaymentRequest;
import kr.hhplus.be.server.domain.payment.interfaces.web.dto.PaymentResponse;
import kr.hhplus.be.server.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentFacade paymentFacade;

    // 결제요청
    @PostMapping("/reservations/{reservationId}/payments")
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(
        @PathVariable Long reservationId,
        @RequestBody PaymentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(
                ApiResponse.success(
                    paymentFacade.processPayment(reservationId, request.getUserId(), request.getAmount())
                )
            );
    }
}
