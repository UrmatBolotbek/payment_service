package faang.school.paymentservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;

@Getter
public class ApiException extends RuntimeException {
    private HttpStatusCode httpStatus;
    public ApiException(String message, HttpStatusCode httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }
}
