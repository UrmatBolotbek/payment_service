package faang.school.paymentservice.service.currency;

import faang.school.paymentservice.config.api.CurrencyConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Component
@Slf4j
public class Conversion {

    private final CurrencyConfig api;

    @Retryable(retryFor = ResourceAccessException.class, maxAttempts = 4,
            backoff = @Backoff(delay = 1000, multiplier = 2))
    public Mono<String> getConversion() {
        WebClient webClient = WebClient.builder().baseUrl("https://api.exchangeratesapi.io/v1").build();
        try {
            return webClient.get().uri(uriBuilder -> uriBuilder.path("/latest")
                    .queryParam("access_key", api.getKey())
                    .build()).retrieve().bodyToMono(String.class);
        } catch (HttpClientErrorException e) {
            log.error("An error occurred while executing a request to an external server. ", e);
            throw new HttpClientErrorException(e.getStatusCode(), e.getResponseBodyAsString());
        }
    }

}
