package faang.school.paymentservice.config.api;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "currency")
public class CurrencyConfig {

    private String key;
    private String endpoint;

}
