package kr.hhplus.be.server.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChargeResponse {
    private Long chargedAmount;
    private Long totalPoints;
}
