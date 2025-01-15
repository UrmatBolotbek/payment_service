package faang.school.paymentservice.service.operation;

import faang.school.paymentservice.model.entity.PendingOperation;
import faang.school.paymentservice.model.enums.OperationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClearingJob {
    private final PendingOperationService pendingOperationService;
    private final OperationMessageService operationMessageService;

    @Scheduled(fixedDelayString = "${app.clearing.job.interval}")
    public void processPendingOperations() {
        OffsetDateTime now = OffsetDateTime.now();
        List<PendingOperation> operationsForClearing = pendingOperationService.getOperationsForClearing(now);

        List<CompletableFuture<Void>> futures = operationsForClearing.stream()
                .map(operation -> CompletableFuture.runAsync(() -> {
                    try {
                        pendingOperationService.confirmOperation(operation.getId(), false);
                        log.info("Operation confirmed by job with ID: {}", operation.getId());
                    } catch (Exception e) {
                        operation.setStatus(OperationStatus.ERROR);
                        operationMessageService.sendOperationMessage(operation);
                        log.error("Failed to confirm operation with ID {}: {}", operation.getId(), e.getMessage());
                    }
                }))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }
}
