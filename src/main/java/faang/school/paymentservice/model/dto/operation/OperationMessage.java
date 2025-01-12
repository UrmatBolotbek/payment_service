package faang.school.paymentservice.model.dto.operation;

import faang.school.paymentservice.model.enums.Category;
import faang.school.paymentservice.model.enums.Currency;
import faang.school.paymentservice.model.enums.OperationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OperationMessage {
    private UUID operationId;
    private UUID sourceAccountId;
    private UUID targetAccountId;
    private String idempotencyKey;
    private BigDecimal amount;
    private Currency currency;
    private Category category;
    private OperationStatus status;
}
