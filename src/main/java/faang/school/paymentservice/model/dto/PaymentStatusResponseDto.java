package faang.school.paymentservice.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
public class PaymentStatusResponseDto {
    private UUID operationId;
    private PaymentStatus paymentStatus;
}
