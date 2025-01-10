package faang.school.paymentservice.repository;

import faang.school.paymentservice.model.entity.PendingOperation;
import faang.school.paymentservice.model.enums.OperationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PendingOperationRepository extends JpaRepository<PendingOperation, UUID> {
    Optional<PendingOperation> findByIdAndStatus(UUID id, OperationStatus status);

    List<PendingOperation> findByStatusAndClearScheduledAtBefore(OperationStatus status, OffsetDateTime dateTime);

    Optional<PendingOperation> findByIdempotencyKey(String idempotencyKey);
}