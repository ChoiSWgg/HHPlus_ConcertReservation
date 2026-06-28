package kr.hhplus.be.server.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL) // 필드가 null일 때 JSON 결과에서 자동으로 제외
public class ApiResponse<T> {
    private final boolean success;
    private final T data;
    private final ErrorDetails error;

    private ApiResponse(boolean success, T data, ErrorDetails error) {
        this.success = success;
        this.data = data;
        this.error = error;
    }

    // 성공 응답 (데이터가 있는 경우)
    public static<T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    // 성공 응답 (데이터가 없는 경우)
    public static<T> ApiResponse<T> success() {
        return new ApiResponse<T>(true, null, null);
    }

    // 실패 응답
    public static ApiResponse<Void> fail(String code, String message) {
        return new ApiResponse<> (false, null, new ErrorDetails(code, message));
    }

    @Getter
    public static class ErrorDetails {
        private final String code;
        private final String message;

        public ErrorDetails(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }
}
