package faang.school.paymentservice.integration.service;

import faang.school.paymentservice.mapper.PendingOperationMapper;
import faang.school.paymentservice.model.dto.operation.CheckingPaymentStatusAndBalance;
import faang.school.paymentservice.model.dto.operation.OperationMessage;
import faang.school.paymentservice.model.dto.operation.PendingOperationDto;
import faang.school.paymentservice.model.entity.PendingOperation;
import faang.school.paymentservice.model.enums.AccountBalanceStatus;
import faang.school.paymentservice.model.enums.OperationStatus;
import faang.school.paymentservice.model.enums.PaymentStatus;
import faang.school.paymentservice.publisher.EventPublisher;
import faang.school.paymentservice.repository.PendingOperationRepository;
import faang.school.paymentservice.service.operation.PendingOperationService;
import faang.school.paymentservice.util.BaseContextTest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class PendingOperationIntegrationTest extends BaseContextTest {
    @Autowired
    private PendingOperationService pendingOperationService;

    @Autowired
    private PendingOperationRepository pendingOperationRepository;

    @Autowired
    private PendingOperationMapper pendingOperationMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @MockBean
    private EventPublisher<OperationMessage> eventPublisher;

    @Test
    public void testInitiateOperation_SavesEntityAndPublishesMessage() {
        PendingOperationDto operationDto = PendingOperationDto.builder()
                .sourceAccountId(UUID.randomUUID())
                .targetAccountId(UUID.randomUUID())
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .category("CHARITY")
                .clearScheduledAt(OffsetDateTime.now().plusDays(1))
                .build();

        UUID operationId = pendingOperationService.initiateOperation(operationDto);

        PendingOperation savedOperation = pendingOperationRepository.findById(operationId).orElse(null);
        assertNotNull(savedOperation);
        assertTrue(operationDto.getAmount().compareTo(savedOperation.getAmount()) == 0);
        assertEquals(operationDto.getCurrency(), savedOperation.getCurrency().name());
        assertEquals(operationDto.getCategory(), savedOperation.getCategory().name());

        ArgumentCaptor<OperationMessage> messageCaptor = ArgumentCaptor.forClass(OperationMessage.class);
        verify(eventPublisher, times(1)).publish(messageCaptor.capture());

        OperationMessage publishedMessage = messageCaptor.getValue();
        assertNotNull(publishedMessage);
        assertEquals(operationId, publishedMessage.getOperationId());
        assertEquals(operationDto.getAmount(), publishedMessage.getAmount());

        CheckingPaymentStatusAndBalance balanceEvent = CheckingPaymentStatusAndBalance.builder()
                .operationId(operationId)
                .status(AccountBalanceStatus.SUFFICIENT_FUNDS)
                .paymentStatus(PaymentStatus.SUCCESS)
                .build();
        redisTemplate.convertAndSend("auth-payment-response", balanceEvent);

        Awaitility.await()
                .atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    PendingOperation updatedOperation = pendingOperationRepository.findById(operationId).orElse(null);
                    assertNotNull(updatedOperation);
                    assertEquals(AccountBalanceStatus.SUFFICIENT_FUNDS, updatedOperation.getAccountBalanceStatus());
                    assertEquals(OperationStatus.AUTHORIZATION, updatedOperation.getStatus());
                });
    }
}