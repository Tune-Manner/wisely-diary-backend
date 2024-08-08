package tuneandmanner.wiselydiarybackend.common.exception;

import tuneandmanner.wiselydiarybackend.common.exception.type.ExceptionCode;

public class DocumentUploadException extends CustomException {
    public DocumentUploadException(final ExceptionCode exceptionCode) {
        super(exceptionCode);
    }
}