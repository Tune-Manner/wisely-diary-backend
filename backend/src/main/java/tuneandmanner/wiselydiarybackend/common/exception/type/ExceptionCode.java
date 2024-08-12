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

    // 400 Error
    CLIENT_ERROR(400, "클라이언트 에러가 발생했습니다."),
    DOCUMENT_UPLOAD_ERROR(400, "문서 업로드 중 오류가 발생했습니다."),

    // 401 Error
    UNAUTHORIZED(401, "인증 되지 않은 요청입니다."),

    // 403 Error
    ACCESS_DENIED(403, "허가 되지 않은 요청입니다."),

    // 404 Error
    NOT_FOUND_SUMMARY_CODE(404, "유효한 요약 코드가 아닙니다."),
    NOT_FOUND_CLIP_ID(404, "유효한 음악 코드가 아닙니다."),
    NOT_FOUND_LETTER_CODE(404, "유효한 편지 코드가 아닙니다."),
    NOT_FOUND_CLIP_URL(404, "클립 URL을 찾을 수 없습니다."),

    // 500 Error
    SERVER_ERROR(500, "서버 에러가 발생했습니다."),
    FAILED_TO_GET_MUSIC_PLAYBACK(500, "음악 조회에 실패했습니다."),
    // Unknown Error
    UNKNOWN_ERROR(520, "알 수 없는 오류가 발생했습니다.");


    private final int code;
    private final String message;

}
