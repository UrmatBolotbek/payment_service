package faang.school.paymentservice.scheduled;

import faang.school.paymentservice.service.currency.CurrencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CurrencyRateFetcher {

    private final CurrencyService currencyService;

    @Scheduled(cron = "${currency-api.cron}")
    public void conversionOfCurrency() {
        log.info("Scheduled task started: Fetching currency rates");
        currencyService.fetchAndSaveRates();
    }
}
