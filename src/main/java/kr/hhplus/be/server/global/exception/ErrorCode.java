package kr.hhplus.be.server.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/*
INVALID_PARAMETER	400	잘못된 파라미터
MISSING_PARAMETER	400	필수 파라미터 누락
UNAUTHORIZED	401	인증 실패 (로그인 필요)
FORBIDDEN	403	계정에 권한 없음
NOT_FOUND	404	리소스를 찾을 수 없음
CONFLICT	409	리소스 충돌
GONE	410	리소스가 있었으나 사라짐 (만료)
INTERNAL_SERVER_ERROR	500	서버 내부 오류
*/

@Getter
public enum ErrorCode {

    // [공통 표준 에러]
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "INVALID_PARAMETER", "잘못된 파라미터입니다."),
    MISSING_PARAMETER(HttpStatus.BAD_REQUEST, "MISSING_PARAMETER", "필수 파라미터가 누락되었습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증 실패 (토큰이 누락되었거나 만료됨)."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "해당 리소스에 대한 권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND", "리소스를 찾을 수 없습니다."),
    CONFLICT(HttpStatus.CONFLICT, "CONFLICT", "리소스 충돌이 발생했습니다."),
    GONE(HttpStatus.GONE, "GONE", "리소스가 만료되었습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다."),

    // [도메인별 커스텀 에러]

    // 1 예약 가능 날짜 조회
    CONCERT_NOT_FOUND(HttpStatus.NOT_FOUND, "CONCERT_NOT_FOUND", "존재하지 않는 콘서트입니다."),

    // 2 특정 날짜 좌석 조회
    CONCERT_SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "CONCERT_SCHEDULE_NOT_FOUND", "해당 콘서트의 일정을 찾을 수 없습니다."),


    // 3 좌석 임시 배정 관련
    SEAT_ALREADY_HELD(HttpStatus.CONFLICT, "SEAT_ALREADY_HELD", "해당 좌석은 이미 임시 배정되었습니다."),
    SEAT_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "SEAT_NOT_AVAILABLE", "해당 좌석은 예약할 수 없습니다."),

    // 4 사용자 포인트 관리 관련
    INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "INVALID_AMOUNT", "충전 금액은 0보다 커야 합니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "해당 사용자를 찾을 수 없습니다."),

    // 5 결제 관련
    INSUFFICIENT_POINTS(HttpStatus.BAD_REQUEST, "INSUFFICIENT_POINTS", "포인트가 부족합니다."),
    RESERVATION_EXPIRED(HttpStatus.GONE, "RESERVATION_EXPIRED", "임시 배정 시간이 만료되었습니다."),

    // 6 유저 대기열 관련
    ALREADY_IN_QUEUE(HttpStatus.CONFLICT, "ALREADY_IN_QUEUE", "이미 대기열에 등록된 사용자입니다."),
    USER_NOT_IN_QUEUE(HttpStatus.NOT_FOUND, "USER_NOT_IN_QUEUE", "대기열에 등록되지 않은 사용자입니다."),
    TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "TOKEN_NOT_FOUND", "해당 사용자의 토큰을 찾을 수 없거나 만료되었습니다."),
    QUEUE_NOT_ACTIVE(HttpStatus.FORBIDDEN, "QUEUE_NOT_ACTIVE", "대기열 순번이 아직 활성화되지 않았습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    // 생성자
    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
