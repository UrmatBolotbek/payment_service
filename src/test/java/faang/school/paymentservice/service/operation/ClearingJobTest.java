package faang.school.paymentservice.service.operation;

import faang.school.paymentservice.model.entity.PendingOperation;
import faang.school.paymentservice.model.enums.AccountBalanceStatus;
import faang.school.paymentservice.model.enums.Category;
import faang.school.paymentservice.model.enums.Currency;
import faang.school.paymentservice.model.enums.OperationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClearingJobTest {
    @Mock
    private PendingOperationService pendingOperationService;
    @Mock
    private OperationMessageService operationMessageService;
    @InjectMocks
    private ClearingJob clearingJob;

    private PendingOperation operation;
    private PendingOperation operationTwo;

    @BeforeEach
    public void setUp() {
        operation = PendingOperation.builder()
                .id(UUID.randomUUID())
                .sourceAccountId(UUID.randomUUID())
                .targetAccountId(UUID.randomUUID())
                .idempotencyKey("1")
                .amount(BigDecimal.ONE)
                .currency(Currency.RUB)
                .status(OperationStatus.PENDING)
                .category(Category.OTHER)
                .accountBalanceStatus(AccountBalanceStatus.SUFFICIENT_FUNDS)
                .clearScheduledAt(OffsetDateTime.now())
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        operationTwo = PendingOperation.builder()
                .id(UUID.randomUUID())
                .sourceAccountId(UUID.randomUUID())
                .targetAccountId(UUID.randomUUID())
                .idempotencyKey("2")
                .amount(BigDecimal.TEN)
                .currency(Currency.RUB)
                .status(OperationStatus.PENDING)
                .category(Category.OTHER)
                .accountBalanceStatus(AccountBalanceStatus.SUFFICIENT_FUNDS)
                .clearScheduledAt(OffsetDateTime.now())
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void testProcessPendingOperations_Success() {
        List<PendingOperation> operations = List.of(operation, operationTwo);
        when(pendingOperationService.getOperationsForClearing(any(OffsetDateTime.class))).thenReturn(operations);

        clearingJob.processPendingOperations();

        verify(pendingOperationService, times(1)).getOperationsForClearing(any(OffsetDateTime.class));
        verify(pendingOperationService, times(1)).confirmOperation(operation.getId(), false);
        verify(pendingOperationService, times(1)).confirmOperation(operationTwo.getId(), false);
        verifyNoInteractions(operationMessageService);
    }

    @Test
    void testProcessPendingOperations_Failure() {
        List<PendingOperation> operations = List.of(operation, operationTwo);
        when(pendingOperationService.getOperationsForClearing(any(OffsetDateTime.class))).thenReturn(operations);
        doThrow(new RuntimeException("Test exception"))
                .when(pendingOperationService).confirmOperation(operation.getId(), false);

        clearingJob.processPendingOperations();

        verify(pendingOperationService,
                times(1)).getOperationsForClearing(any(OffsetDateTime.class));
        verify(pendingOperationService,
                times(1)).confirmOperation(operation.getId(), false);
        verify(pendingOperationService,
                times(1)).confirmOperation(operationTwo.getId(), false);
        verify(operationMessageService,
                times(1)).sendOperationMessage(operation);
        verifyNoMoreInteractions(operationMessageService);
    }
}