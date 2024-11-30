package faang.school.paymentservice.scheduled;

import faang.school.paymentservice.service.currency.CurrencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CurrencyRateFetcher {

    private final CurrencyService currencyService;

    public void conversionOfCurrency() {
        currencyService.cunversionOfCurrency();
    }

}
