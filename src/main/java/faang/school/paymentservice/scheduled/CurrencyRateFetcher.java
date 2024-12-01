package faang.school.paymentservice.scheduled;

import faang.school.paymentservice.service.currency.CurrencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;

import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class CurrencyRateFetcher {

    private final CurrencyService currencyService;

    @Scheduled(cron = "${currency-scheduling.cron}")
    public String conversionOfCurrency() {
       return currencyService.conversionOfCurrency();
    }

}
