package faang.school.paymentservice.model.enums;

public enum OperationStatus {
    PENDING,
    AUTHORIZATION,
    CANCELLATION,
    CLEARING,
    ERROR,
    FINISHED_CLEARING,
    FINISHED_CANCELLATION,
    FINISHED_ERROR
}