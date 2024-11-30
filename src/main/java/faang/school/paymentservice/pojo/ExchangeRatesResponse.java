package faang.school.paymentservice.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExchangeRatesResponse {

    private boolean success;
    private long timestamp;
    private String base;
    private LocalDate date;
    private Map<String, Double> rates;

}
