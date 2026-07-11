package kr.hhplus.be.server.domain.payment.interfaces.web.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PaymentRequest {
    private Long userId;
    private Long amount;
}
