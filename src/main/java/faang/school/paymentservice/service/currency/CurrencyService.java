package faang.school.paymentservice.service.currency;

import faang.school.paymentservice.pojo.ExchangeRatesResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class CurrencyService {

    private final String apiKey = "08a9cf90a2931b1ffea4614703e7f54f";

    private final WebClient webClient;

    public void cunversionOfCurrency() {
        Mono<ExchangeRatesResponse> test = webClient.get().uri(uriBuilder -> uriBuilder.path("/latest")
                .queryParam("access_key", apiKey)
                .build()).retrieve().bodyToMono(ExchangeRatesResponse.class);
        test.block();

        test.subscribe(
                exchangeRatesResponse -> {
                    System.out.println("Base currency: " + exchangeRatesResponse.getBase());
                    exchangeRatesResponse.getRates().forEach((currency, value) -> {
                        System.out.println("Currency: " + currency + ", Rate: " + value);
                    });
                },
                error -> System.err.println("Error encountered: " + error)
        );
    }


}


