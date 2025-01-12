package faang.school.paymentservice.model.dto.operation;

import faang.school.paymentservice.model.enums.AccountBalanceStatus;
import faang.school.paymentservice.model.enums.OperationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingOperationResponseDto {
    private AccountBalanceStatus accountBalanceStatus;
    private OperationStatus status;
}
