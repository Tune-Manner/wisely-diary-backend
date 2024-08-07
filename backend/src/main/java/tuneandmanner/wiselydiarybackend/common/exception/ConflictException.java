package tuneandmanner.wiselydiarybackend.common.exception;

import lombok.Getter;
import tuneandmanner.wiselydiarybackend.common.exception.type.ExceptionCode;

@Getter
public class ConflictException extends CustomException {
    public ConflictException(final ExceptionCode exceptionCode) {
        super(exceptionCode);
    }
}
