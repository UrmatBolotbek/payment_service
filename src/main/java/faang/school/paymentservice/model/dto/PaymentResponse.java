package faang.school.paymentservice.model.dto;

import faang.school.paymentservice.model.enums.Currency;
import faang.school.paymentservice.model.enums.PaymentStatus;

import java.math.BigDecimal;
public record PaymentResponse(
        PaymentStatus status,
        int verificationCode,
        long paymentNumber,
        BigDecimal amount,
        Currency currency,
        String message
) {
}
