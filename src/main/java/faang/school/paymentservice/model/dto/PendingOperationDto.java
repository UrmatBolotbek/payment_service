package faang.school.paymentservice.model.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PendingOperationDto {
    @NotNull(message = "AccountFrom ID is required")
    private UUID sourceAccountId;

    @NotNull(message = "AccountTo ID is required")
    private UUID targetAccountId;

    @NotBlank(message = "Idempotency key is required")
    private String idempotencyKey;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    private String currency;

    @NotBlank(message = "Currency is required")
    private String category;

    @NotNull(message = "Clear scheduled time is required")
    @Future(message = "Clear scheduled time must be in the future")
    private OffsetDateTime clearScheduledAt;
}
