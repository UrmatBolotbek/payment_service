package faang.school.paymentservice.model.dto.operation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import faang.school.paymentservice.model.enums.AccountBalanceStatus;
import faang.school.paymentservice.model.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CheckingPaymentStatusAndBalance {
    private UUID operationId;
    private AccountBalanceStatus status;
    private PaymentStatus paymentStatus;
}

