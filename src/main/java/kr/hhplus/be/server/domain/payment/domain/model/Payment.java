package kr.hhplus.be.server.domain.payment.domain.model;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class Payment {
    private Long id;
    private Long reservationId;
    private Long price;
    private LocalDateTime paidAt;

    private Payment() {}

    // 결제 완료 시 Payment 도메인 객체 생성 (paidAt은 현재 시각으로 자동 설정)
    public static Payment of(Long reservationId, Long price) {
        Payment payment = new Payment();
        payment.reservationId = reservationId;
        payment.price = price;
        payment.paidAt = LocalDateTime.now();
        return payment;
    }

    // Entity -> Domain 매핑
    public static Payment reconstruct(Long id, Long reservationId, Long price, LocalDateTime paidAt) {
        Payment payment = new Payment();
        payment.id = id;
        payment.reservationId = reservationId;
        payment.price = price;
        payment.paidAt = paidAt;
        return payment;
    }
}
