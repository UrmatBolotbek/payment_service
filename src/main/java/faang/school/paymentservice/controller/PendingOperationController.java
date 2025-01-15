package faang.school.paymentservice.controller;

import faang.school.paymentservice.model.dto.operation.PendingOperationDto;
import faang.school.paymentservice.model.dto.operation.PendingOperationResponseDto;
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
@RequestMapping("/api/v1/operations")
@RestController
public class PendingOperationController {
    private final PendingOperationService pendingOperationService;

    @PostMapping("/initiate")
    public ResponseEntity<UUID> initiateOperation(@RequestBody PendingOperationDto operationDto) {
        UUID operationId = pendingOperationService.initiateOperation(operationDto);
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
