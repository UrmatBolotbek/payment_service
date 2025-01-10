package faang.school.paymentservice.validator;

import faang.school.paymentservice.exception.InvalidOperationException;
import faang.school.paymentservice.model.entity.PendingOperation;
import faang.school.paymentservice.model.enums.OperationStatus;
import faang.school.paymentservice.repository.PendingOperationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class PendingOperationValidator {
    private final PendingOperationRepository pendingOperationRepository;

    public void validateIdempotencyKey(String idempotencyKey) {
        Optional<PendingOperation> existingOperation = pendingOperationRepository.findByIdempotencyKey(idempotencyKey);
        if (existingOperation.isPresent()) {
            throw new InvalidOperationException("Operation with the same idempotency key already exists");
        }
    }

    public void validateManualConfirmation(PendingOperation operation) {
        if (operation.getStatus() != OperationStatus.AUTHORIZATION) {
            throw new InvalidOperationException("Operation cannot be manually confirmed as it is not in AUTHORIZATION status");
        }
    }

    public void validateAutomaticConfirmation(PendingOperation operation) {
        if (operation.getStatus() != OperationStatus.AUTHORIZATION) {
            throw new InvalidOperationException("Operation cannot be automatically confirmed as it is not in AUTHORIZATION status");
        }
        if (operation.getClearScheduledAt().isAfter(OffsetDateTime.now())) {
            throw new InvalidOperationException("Cannot automatically confirm operation before scheduled time");
        }
    }
}
