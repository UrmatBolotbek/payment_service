package faang.school.paymentservice.service.operation;

import faang.school.paymentservice.exception.ErrorOperationException;
import faang.school.paymentservice.model.dto.operation.PaymentStatusResponseDto;
import faang.school.paymentservice.model.entity.PendingOperation;
import faang.school.paymentservice.model.enums.OperationStatus;
import faang.school.paymentservice.repository.PendingOperationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckPaymentStatusService {
    private final PendingOperationRepository pendingOperationRepository;

    public void checkPaymentStatus(PaymentStatusResponseDto event) {
        PendingOperation pendingOperation = getPendingOperation(event.getOperationId());

        switch (event.getPaymentStatus()) {
            case FAILED -> handleErrorStatus(pendingOperation);
            case SUCCESS -> handleSuccessStatus(pendingOperation, event);
            default -> log.warn("Unhandled payment status {} for operation {}",
                    event.getPaymentStatus(), event.getOperationId());
        }
    }

    private PendingOperation getPendingOperation(UUID operationId) {
        return pendingOperationRepository.findById(operationId)
                .orElseThrow(() -> new IllegalArgumentException("Operation not found: " + operationId));
    }

    private void handleErrorStatus(PendingOperation operation) {
        switch (operation.getStatus()) {
            case CANCELLATION, ERROR -> {
                updateOperationStatus(operation, OperationStatus.ERROR);
                throw new ErrorOperationException("Error occurred during the cancellation operation.");
            }
            case CLEARING -> {
                updateOperationStatus(operation, OperationStatus.ERROR);
                throw new ErrorOperationException("Error occurred during the clearing operation.");
            }
            default -> log.warn("Unhandled operation status {} for error event on operation {}",
                    operation.getStatus(), operation.getId());
        }
    }

    private void handleSuccessStatus(PendingOperation operation, PaymentStatusResponseDto event) {
        switch (operation.getStatus()) {
            case CANCELLATION -> {
                updateOperationStatus(operation, OperationStatus.FINISHED_CANCELLATION);
                log.info("Payment with id: {} was cancelled", event.getOperationId());
            }
            case ERROR -> {
                updateOperationStatus(operation, OperationStatus.FINISHED_ERROR);
                log.info("Payment with id: {} was aborted", event.getOperationId());
            }
            case CLEARING -> {
                updateOperationStatus(operation, OperationStatus.FINISHED_CLEARING);
                log.info("Payment with id: {} was successful", event.getOperationId());
            }
            default -> log.warn("Unexpected operation status {} for successful event on operation {}",
                    operation.getStatus(), operation.getId());
        }
    }

    private void updateOperationStatus(PendingOperation operation, OperationStatus newStatus) {
        operation.setStatus(newStatus);
        pendingOperationRepository.save(operation);
    }
}