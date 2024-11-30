package faang.school.paymentservice.scheduled;

import faang.school.paymentservice.service.currency.CurrencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.ResourceAccessException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class CurrencyRateFetcher {

    private final CurrencyService currencyService;

    @PostMapping("/test")
    @Retryable(retryFor = ResourceAccessException.class, maxAttempts = 4,
            backoff = @Backoff(delay = 1000, multiplier = 2))
    public void conversionOfCurrency() {
        currencyService.cunversionOfCurrency();
    }

}
