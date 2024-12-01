package faang.school.paymentservice.service.currency;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
public class CurrencyServiceTest {

    @InjectMocks
    private CurrencyService currencyService;
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private Conversion conversion;
    @Mock
    ValueOperations<String, String> ops;

    private Mono<String> response;

    @Test
    void testConversionOfCurrencySuccess() {
        String JSON = "{\"success\": true," +
                " \"timestamp\": 1519296206," +
                " \"base\": \"EUR\"," +
                " \"date\": \"2024-12-01\"," +
                " \"rates\": { \"AUD\": 1.566015," +
                " \"CAD\": 1.560132," +
                " \"CHF\": 1.154727," +
                " \"USD\": 1.23396 }}";
        response = Mono.just(JSON);
        when(conversion.getConversion()).thenReturn(response);
        when(redisTemplate.opsForValue()).thenReturn(ops);

        String key = currencyService.conversionOfCurrency();

        verify(ops).set(anyString(), anyString(), eq(1L), eq(TimeUnit.DAYS));
        assertEquals(key, "exchangeRates:\"2024-12-01\"");
    }

    @Test
    void testConversionOfCurrencyWithException() {
        String JSON = "{\"success\": false," +
                " \"error\" : " +
                "{\"code\": 104," +
                " \"info\": Your monthly API request volume has been reached. Please upgrade your plan.}}";
        response = Mono.just(JSON);
        assertThrows(RuntimeException.class, () -> currencyService.conversionOfCurrency());

    }
}
