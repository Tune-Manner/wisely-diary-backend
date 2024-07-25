package tuneandmanner.wiselydiarybackend.common.exception;

import lombok.Getter;
import tuneandmanner.wiselydiarybackend.common.exception.type.ExceptionCode;

@Getter
public class ServerInternalException extends CustomException {
    public ServerInternalException(final ExceptionCode exceptionCode) {
        super(exceptionCode);
    }
}
