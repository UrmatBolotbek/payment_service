package faang.school.paymentservice.scheduled;

import faang.school.paymentservice.service.currency.CurrencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class CurrencyRateFetcher {

    private final CurrencyService currencyService;

    @PostMapping("/test")
    public void conversionOfCurrency() {
        currencyService.cunversionOfCurrency();
    }

}
