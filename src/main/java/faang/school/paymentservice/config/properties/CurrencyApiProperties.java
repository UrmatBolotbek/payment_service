package faang.school.paymentservice.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "currency-api")
public class CurrencyApiProperties {
    private String url;
    private String apiKey;
    private String redisKey;
    private String baseCurrency;
    private Timeout timeout = new Timeout();

    @Getter
    @Setter
    public static class Timeout {
        private int connection;
        private int read;
    }
}
