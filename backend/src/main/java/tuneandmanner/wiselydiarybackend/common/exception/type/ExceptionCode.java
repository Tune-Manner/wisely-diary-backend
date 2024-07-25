package tuneandmanner.wiselydiarybackend.common.exception.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ExceptionCode {

    /*
     * 401: Unauthorized
     * 403: Forbidden
     * 404: Not Found
     * 405: Method Not Allowed
     * 409: Conflict
     */

    // 401 Error
    UNAUTHORIZED(401, "인증 되지 않은 요청입니다."),

    // 403 Error
    ACCESS_DENIED(403, "허가 되지 않은 요청입니다."),

    // 404 Error
    NOT_FOUND_TASK_ID(404, "유효한 음악 코드가 아닙니다.");

    // 409 Error


    private final int code;
    private final String message;

}
