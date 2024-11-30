package faang.school.paymentservice.service.currency;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import faang.school.paymentservice.pojo.ExchangeRates;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import redis.clients.jedis.Jedis;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class CurrencyService {

    private final String apiKey = "08a9cf90a2931b1ffea4614703e7f54f";

    private final WebClient webClient;
    private final RedisTemplate<String, String> redisTemplate;

    public void cunversionOfCurrency() {
        try {
            Mono<ExchangeRates> test = webClient.get().uri(uriBuilder -> uriBuilder.path("/latest")
                    .queryParam("access_key", apiKey)
                    .queryParam("base", "RUB")
                    .build()).retrieve().bodyToMono(ExchangeRates.class);
            ExchangeRates exchangeRates = test.block();
            if (exchangeRates.isSuccess()) {}
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            String jsonString = objectMapper.writeValueAsString(exchangeRates);
            String key = "exchangeRates:" + exchangeRates.getDate().toString();
            redisTemplate.opsForValue().set(key, jsonString, 1, TimeUnit.DAYS);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        } catch (HttpClientErrorException e) {
            log.error("An error occurred while executing a request to an external server ", e);
            throw new HttpClientErrorException(e.getStatusCode(), e.getResponseBodyAsString());
        }

    }


}


