package kr.hhplus.be.server.domain.payment.interfaces.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentResponse {
    private Long paymentId;
    private Long amount;
    private String status;
}
