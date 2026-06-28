package kr.hhplus.be.server.global.response;

public class ApiResponse<T> {
    private boolean success;
    private T data;
    private ErrorResponse error;

    public static class ErrorResponse {
        private String code;
        private String message;
    }
}
