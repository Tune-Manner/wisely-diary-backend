package tuneandmanner.wiselydiarybackend.common.exception;

import lombok.Getter;
import tuneandmanner.wiselydiarybackend.common.exception.type.ExceptionCode;

@Getter
public class NotFoundException extends CustomException {
    public NotFoundException(final ExceptionCode exceptionCode) {
        super(exceptionCode);
    }

}
