package faang.school.paymentservice.exception;

import org.springframework.http.HttpStatus;

public class OperationNotFoundException extends ApiException {
    public OperationNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
