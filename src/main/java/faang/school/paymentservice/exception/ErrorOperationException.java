package faang.school.paymentservice.exception;

import org.springframework.http.HttpStatus;

public class ErrorOperationException extends ApiException {
    public ErrorOperationException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
