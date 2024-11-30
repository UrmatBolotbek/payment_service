package faang.school.paymentservice.service.currency;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;


import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class CurrencyService {

    private final RedisTemplate<String, String> redisTemplate;
    private final Conversion conversion;

    public String conversionOfCurrency() {
        try {
            Mono<String> conversionResponse = conversion.getConversion();
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            JsonNode rootNode = mapper.readTree(conversionResponse.block());
            boolean success = rootNode.path("success").asBoolean();
            if (!success) {
                JsonNode errorNode = rootNode.path("error");
                int errorCode = errorNode.path("code").asInt();
                String errorMessage = errorNode.path("info").asText();
                log.error("The method encountered an error {} in the JSON returned" +
                        " from an external service when converting currencies. {} ",errorCode, errorMessage);
                throw new RuntimeException(errorMessage);
            }
            String key = "exchangeRates:" + rootNode.path("date");
            redisTemplate.opsForValue().set(key, Objects.requireNonNull(conversionResponse.block()), 1, TimeUnit.DAYS);
            return key;
        } catch (JsonProcessingException e) {
            log.error("An error occurred while processing JSON content. ", e);
            throw new JSONException(e);
        }
    }

}


