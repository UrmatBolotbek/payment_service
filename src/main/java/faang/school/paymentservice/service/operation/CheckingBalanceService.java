package faang.school.paymentservice.service.operation;

import faang.school.paymentservice.exception.ErrorOperationException;
import faang.school.paymentservice.exception.InsufficientBalanceException;
import faang.school.paymentservice.model.dto.operation.CheckingPaymentStatusAndBalance;
import faang.school.paymentservice.model.entity.PendingOperation;
import faang.school.paymentservice.model.enums.AccountBalanceStatus;
import faang.school.paymentservice.model.enums.OperationStatus;
import faang.school.paymentservice.model.enums.PaymentStatus;
import faang.school.paymentservice.repository.PendingOperationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckingBalanceService {
    private final PendingOperationRepository pendingOperationRepository;

    public void checkBalance(CheckingPaymentStatusAndBalance event, AccountBalanceStatus status) {
        PendingOperation pendingOperation = getPendingOperation(event.getOperationId());

        switch (status) {
            case INSUFFICIENT_FUNDS -> handleInsufficientFunds(pendingOperation, event);
            case SUFFICIENT_FUNDS -> handleSufficientFunds(pendingOperation, event);
            default -> log.warn("Unhandled balance status {} for operation {}", status, event.getOperationId());
        }
    }

    private PendingOperation getPendingOperation(UUID operationId) {
        return pendingOperationRepository.findById(operationId)
                .orElseThrow(() -> new IllegalArgumentException("Operation not found: " + operationId));
    }

    private void handleInsufficientFunds(PendingOperation operation, CheckingPaymentStatusAndBalance event) {
        if (event.getPaymentStatus() == PaymentStatus.FAILED) {
            updateOperationStatus(operation, OperationStatus.ERROR, AccountBalanceStatus.INSUFFICIENT_FUNDS);
            throw new InsufficientBalanceException("Not enough funds on the account for the requested operation.");
        }
    }

    private void handleSufficientFunds(PendingOperation operation, CheckingPaymentStatusAndBalance event) {
        switch (event.getPaymentStatus()) {
            case FAILED -> {
                updateOperationStatus(operation, OperationStatus.ERROR, AccountBalanceStatus.SUFFICIENT_FUNDS);
                throw new ErrorOperationException("Error occurred during the operation.");
            }
            case SUCCESS -> {
                updateOperationStatus(operation, OperationStatus.AUTHORIZATION, AccountBalanceStatus.SUFFICIENT_FUNDS);
                log.info("Account balance is sufficient for the operation with id: {}", operation.getId());
            }
            default ->
                    log.warn("Unhandled payment status {} for operation {}", event.getPaymentStatus(), event.getOperationId());
        }
    }

    private void updateOperationStatus(PendingOperation operation, OperationStatus newStatus, AccountBalanceStatus balanceStatus) {
        operation.setStatus(newStatus);
        operation.setAccountBalanceStatus(balanceStatus);
        pendingOperationRepository.save(operation);
    }
}
