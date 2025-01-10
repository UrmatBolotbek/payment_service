package faang.school.paymentservice.controller;

import faang.school.paymentservice.model.dto.PendingOperationDto;
import faang.school.paymentservice.model.dto.PendingOperationResponseDto;
import faang.school.paymentservice.mapper.PendingOperationMapper;
import faang.school.paymentservice.model.entity.PendingOperation;
import faang.school.paymentservice.service.operation.PendingOperationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RequiredArgsConstructor
@RequestMapping("/operations")
@RestController
public class PendingOperationController {
    private final PendingOperationService pendingOperationService;
    private final PendingOperationMapper pendingOperationMapper;

    @PostMapping("/initiate")
    public ResponseEntity<UUID> initiateOperation(@RequestBody PendingOperationDto operationDto) {
        PendingOperation operation = pendingOperationMapper.toEntity(operationDto);
        UUID operationId = pendingOperationService.initiateOperation(operation);
        return ResponseEntity.ok(operationId);
    }

    @PostMapping("/cancel/{id}")
    public ResponseEntity<Void> cancelOperation(@PathVariable UUID id) {
        pendingOperationService.cancelOperation(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/confirm/{id}")
    public ResponseEntity<Void> confirmOperation(@PathVariable UUID id) {
        pendingOperationService.confirmOperation(id, true);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/{id}")
    public ResponseEntity<PendingOperationResponseDto> getOperationStatus(@PathVariable UUID id) {
        PendingOperationResponseDto responseDto = pendingOperationService.getOperationStatuses(id);
        return ResponseEntity.ok(responseDto);
    }
}
