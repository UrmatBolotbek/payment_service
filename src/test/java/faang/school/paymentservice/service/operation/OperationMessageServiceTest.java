package faang.school.paymentservice.service.operation;

import faang.school.paymentservice.model.dto.operation.OperationMessage;
import faang.school.paymentservice.model.entity.PendingOperation;
import faang.school.paymentservice.model.enums.Category;
import faang.school.paymentservice.model.enums.Currency;
import faang.school.paymentservice.model.enums.OperationStatus;
import faang.school.paymentservice.publisher.EventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OperationMessageServiceTest {
    @Mock
    private EventPublisher<OperationMessage> eventPublisher;
    @InjectMocks
    private OperationMessageService operationMessageService;

    @Test
    void testSendOperationMessage_Success() {
        UUID operationId = UUID.randomUUID();
        PendingOperation pendingOperation = PendingOperation.builder()
                .id(operationId)
                .sourceAccountId(UUID.randomUUID())
                .targetAccountId(UUID.randomUUID())
                .idempotencyKey(UUID.randomUUID().toString())
                .amount(BigDecimal.TEN)
                .currency(Currency.RUB)
                .category(Category.OTHER)
                .status(OperationStatus.PENDING)
                .build();

        operationMessageService.sendOperationMessage(pendingOperation);

        ArgumentCaptor<OperationMessage> messageCaptor = ArgumentCaptor.forClass(OperationMessage.class);
        verify(eventPublisher, times(1)).publish(messageCaptor.capture());

        OperationMessage publishedMessage = messageCaptor.getValue();
        assertEquals(pendingOperation.getId(), publishedMessage.getOperationId());
        assertEquals(pendingOperation.getSourceAccountId(), publishedMessage.getSourceAccountId());
        assertEquals(pendingOperation.getTargetAccountId(), publishedMessage.getTargetAccountId());
        assertEquals(pendingOperation.getIdempotencyKey(), publishedMessage.getIdempotencyKey());
        assertEquals(pendingOperation.getAmount(), publishedMessage.getAmount());
        assertEquals(pendingOperation.getCurrency(), publishedMessage.getCurrency());
        assertEquals(pendingOperation.getCategory(), publishedMessage.getCategory());
        assertEquals(pendingOperation.getStatus(), publishedMessage.getStatus());
    }

    @Test
    void testSendOperationMessage_NullOperation() {
        assertThrows(NullPointerException.class, () -> operationMessageService.sendOperationMessage(null));
        verify(eventPublisher, never()).publish(any(OperationMessage.class));
    }
}