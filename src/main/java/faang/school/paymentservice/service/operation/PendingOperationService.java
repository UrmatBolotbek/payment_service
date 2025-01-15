package faang.school.paymentservice.service.operation;

import faang.school.paymentservice.exception.OperationNotFoundException;
import faang.school.paymentservice.mapper.PendingOperationMapper;
import faang.school.paymentservice.model.dto.operation.PendingOperationDto;
import faang.school.paymentservice.model.dto.operation.PendingOperationResponseDto;
import faang.school.paymentservice.model.entity.PendingOperation;
import faang.school.paymentservice.model.enums.OperationStatus;
import faang.school.paymentservice.repository.PendingOperationRepository;
import faang.school.paymentservice.validator.PendingOperationValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PendingOperationService {
    private final PendingOperationMapper pendingOperationMapper;
    private final PendingOperationRepository pendingOperationRepository;
    private final PendingOperationValidator pendingOperationValidator;
    private final OperationMessageService operationMessageService;

    @Transactional
    public UUID initiateOperation(PendingOperationDto operationDto) {
        PendingOperation operation = pendingOperationMapper.toEntity(operationDto);
        pendingOperationValidator.validateIdempotencyKey(operation.getIdempotencyKey());
        pendingOperationRepository.saveAndFlush(operation);
        operationMessageService.sendOperationMessage(operation);
        log.info("Operation initiated with ID: {}", operation.getId());
        return operation.getId();
    }

    @Transactional
    public void cancelOperation(UUID operationId) {
        PendingOperation operation = getOperationForProcessing(operationId);
        updateOperationStatus(operation, OperationStatus.CANCELLATION);
        operationMessageService.sendOperationMessage(operation);
        log.info("Operation canceled with ID: {}", operationId);
    }

    @Transactional
    public void confirmOperation(UUID operationId, boolean isManual) {
        PendingOperation operation = getOperationForProcessing(operationId);
        try {
            if (isManual) {
                pendingOperationValidator.validateManualConfirmation(operation);
            } else {
                pendingOperationValidator.validateAutomaticConfirmation(operation);
            }
            updateOperationStatus(operation, OperationStatus.CLEARING);
            operationMessageService.sendOperationMessage(operation);
            log.info("Operation confirmed with ID: {}", operationId);
        } catch (Exception e) {
            log.error("Error during operation confirmation for ID {}: {}", operationId, e.getMessage(), e);
            updateOperationStatus(operation, OperationStatus.ERROR);
            operationMessageService.sendOperationMessage(operation);
        }
    }

    @Transactional(readOnly = true)
    public PendingOperationResponseDto getOperationStatuses(UUID operationId) {
        return pendingOperationRepository.findById(operationId)
                .map(operation -> PendingOperationResponseDto.builder()
                        .accountBalanceStatus(operation.getAccountBalanceStatus())
                        .status(operation.getStatus())
                        .build())
                .orElseThrow(() -> new OperationNotFoundException("Operation not found"));
    }

    private void updateOperationStatus(PendingOperation operation, OperationStatus status) {
        operation.setStatus(status);
        pendingOperationRepository.save(operation);
    }

    public List<PendingOperation> getOperationsForClearing(OffsetDateTime currentTime) {
        return pendingOperationRepository.findByStatusAndClearScheduledAtBefore(OperationStatus.AUTHORIZATION, currentTime);
    }

    private PendingOperation getOperationForProcessing(UUID operationId) {
        return pendingOperationRepository.findByIdAndStatus(operationId, OperationStatus.AUTHORIZATION)
                .orElseThrow(() -> new OperationNotFoundException("Operation not found or in invalid status"));
    }
}