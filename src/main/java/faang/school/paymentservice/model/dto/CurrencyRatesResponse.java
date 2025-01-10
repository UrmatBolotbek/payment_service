package faang.school.paymentservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CurrencyRatesResponse {
    private boolean success;
    private long timestamp;
    private String base;
    private String date;
    private Map<String, BigDecimal> rates;
}
