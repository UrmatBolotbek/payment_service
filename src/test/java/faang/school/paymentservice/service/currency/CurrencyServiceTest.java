package faang.school.paymentservice.service.currency;

import faang.school.paymentservice.config.properties.CurrencyApiProperties;
import faang.school.paymentservice.config.properties.RetryProperties;
import faang.school.paymentservice.exception.CurrencyRateException;
import faang.school.paymentservice.model.dto.currency.CurrencyRatesResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CurrencyServiceTest {
    @InjectMocks
    private CurrencyService currencyService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private WebClient currencyWebClient;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private CurrencyApiProperties currencyApiProperties;

    @Mock
    private RetryProperties retryProperties;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private static final String BASE_CURRENCY = "EUR";
    private static final String URL = "https://api.exchangeratesapi.io/v1/";
    private static final String API_KEY = "QN0WSBBHFMfIp45JdHUyuA9gYRfphUi5";
    private static final String REDIS_KEY = "currencyRates";
    private static final int RETRY_COUNT = 2;
    private static final int RETRY_DELAY = 1000;
    private static final int READ_TIMEOUT = 1000;
    private CurrencyRatesResponse currencyRatesResponse;

    @BeforeEach
    public void setUp() {
        currencyRatesResponse = CurrencyRatesResponse.builder()
                .success(true)
                .base(BASE_CURRENCY)
                .rates(new HashMap<>())
                .build();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    public void testFetchAndSaveRates_Success() {
        CurrencyApiProperties.Timeout timeoutProperties = mock(CurrencyApiProperties.Timeout.class);
        when(timeoutProperties.getRead()).thenReturn(READ_TIMEOUT);

        when(currencyApiProperties.getUrl()).thenReturn(URL);
        when(currencyApiProperties.getApiKey()).thenReturn(API_KEY);
        when(currencyApiProperties.getTimeout()).thenReturn(timeoutProperties);
        when(currencyApiProperties.getRedisKey()).thenReturn(REDIS_KEY);

        when(retryProperties.getRetry()).thenReturn(RETRY_COUNT);
        when(retryProperties.getDelay()).thenReturn(RETRY_DELAY);

        when(currencyWebClient.get()
                .uri(anyString())
                .retrieve()
                .bodyToMono(CurrencyRatesResponse.class))
                .thenReturn(Mono.just(currencyRatesResponse));

        CurrencyRatesResponse result = currencyService.fetchAndSaveRates();

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(BASE_CURRENCY, result.getBase());

        verify(redisTemplate.opsForValue(), times(1)).set(REDIS_KEY, currencyRatesResponse);
    }

    @Test
    public void testFetchAndSaveRates_Failure() {
        String errorMessage = "API error";

        CurrencyApiProperties.Timeout timeoutProperties = mock(CurrencyApiProperties.Timeout.class);
        when(timeoutProperties.getRead()).thenReturn(READ_TIMEOUT);

        when(currencyApiProperties.getUrl()).thenReturn(URL);
        when(currencyApiProperties.getApiKey()).thenReturn(API_KEY);
        when(currencyApiProperties.getTimeout()).thenReturn(timeoutProperties);
        when(currencyApiProperties.getBaseCurrency()).thenReturn(BASE_CURRENCY);

        when(retryProperties.getRetry()).thenReturn(RETRY_COUNT);
        when(retryProperties.getDelay()).thenReturn(RETRY_DELAY);

        when(currencyWebClient.get()
                .uri(anyString())
                .retrieve()
                .bodyToMono(CurrencyRatesResponse.class))
                .thenReturn(Mono.error(new RuntimeException(errorMessage)));

        CurrencyRateException exception = assertThrows(CurrencyRateException.class,
                () -> currencyService.fetchAndSaveRates());

        assertEquals("Temporary we accept only the following currencies: " + BASE_CURRENCY, exception.getMessage());

        verify(redisTemplate.opsForValue(), never()).set(anyString(), any());
    }

    @Test
    public void testGetCurrencyRatesFromRedis_Success() {
        when(currencyApiProperties.getRedisKey()).thenReturn(REDIS_KEY);
        when(valueOperations.get(REDIS_KEY)).thenReturn(currencyRatesResponse);

        CurrencyRatesResponse result = currencyService.getCurrencyRatesFromRedis();

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(BASE_CURRENCY, result.getBase());

        verify(redisTemplate.opsForValue()).get(REDIS_KEY);
    }

    @Test
    public void testGetCurrencyRatesFromRedis_Failure_KeyAbsent() {
        when(currencyApiProperties.getRedisKey()).thenReturn(REDIS_KEY);
        when(valueOperations.get(REDIS_KEY)).thenReturn(null);

        CurrencyApiProperties.Timeout timeoutProperties = mock(CurrencyApiProperties.Timeout.class);
        when(timeoutProperties.getRead()).thenReturn(READ_TIMEOUT);

        when(currencyApiProperties.getUrl()).thenReturn(URL);
        when(currencyApiProperties.getApiKey()).thenReturn(API_KEY);
        when(currencyApiProperties.getTimeout()).thenReturn(timeoutProperties);
        when(currencyApiProperties.getRedisKey()).thenReturn(REDIS_KEY);

        when(retryProperties.getRetry()).thenReturn(RETRY_COUNT);
        when(retryProperties.getDelay()).thenReturn(RETRY_DELAY);

        when(currencyWebClient.get()
                .uri(anyString())
                .retrieve()
                .bodyToMono(CurrencyRatesResponse.class))
                .thenReturn(Mono.just(currencyRatesResponse));

        CurrencyRatesResponse result = currencyService.getCurrencyRatesFromRedis();

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(BASE_CURRENCY, result.getBase());

        verify(redisTemplate.opsForValue()).get(REDIS_KEY);
        verify(redisTemplate.opsForValue()).set(REDIS_KEY, currencyRatesResponse);
    }

    @Test
    public void testGetCurrencyRatesFromRedis_RedisUnavailable() {
        when(currencyApiProperties.getRedisKey()).thenReturn(REDIS_KEY);
        when(currencyApiProperties.getBaseCurrency()).thenReturn(BASE_CURRENCY);

        when(redisTemplate.opsForValue().get(REDIS_KEY)).thenThrow(new RuntimeException("Redis is unavailable"));

        CurrencyRateException exception = assertThrows(CurrencyRateException.class,
                () -> currencyService.getCurrencyRatesFromRedis());

        assertEquals("Temporary we accept only the following currencies: " + BASE_CURRENCY, exception.getMessage());

        verify(redisTemplate.opsForValue()).get(REDIS_KEY);
    }

    @Test
    public void testConvertToBaseCurrency_Success() {
        BigDecimal amount = new BigDecimal("100");
        String sourceCurrency = "RUB";
        BigDecimal exchangeRate = new BigDecimal("102.23");
        currencyRatesResponse.getRates().put(sourceCurrency.toUpperCase(), exchangeRate);

        when(currencyApiProperties.getRedisKey()).thenReturn(REDIS_KEY);
        when(valueOperations.get(REDIS_KEY)).thenReturn(currencyRatesResponse);

        BigDecimal result = currencyService.convertToBaseCurrency(amount, sourceCurrency);

        BigDecimal expected = amount.divide(exchangeRate, 5, RoundingMode.HALF_UP);
        assertEquals(expected, result);

        verify(redisTemplate.opsForValue()).get(REDIS_KEY);
    }

    @Test
    public void testConvertToBaseCurrency_ExchangeRateUnavailable() {
        BigDecimal amount = new BigDecimal("100");
        String sourceCurrency = "RRR";
        String errorMessage = String.format("Exchange rate for currency '%s' is not available. " +
                "Available only following currencies: [USD]", sourceCurrency);
        currencyRatesResponse.getRates().put("USD", new BigDecimal("1.2"));

        when(currencyApiProperties.getRedisKey()).thenReturn(REDIS_KEY);
        when(valueOperations.get(REDIS_KEY)).thenReturn(currencyRatesResponse);

        CurrencyRateException exception = assertThrows(CurrencyRateException.class,
                () -> currencyService.convertToBaseCurrency(amount, sourceCurrency));

        assertEquals(errorMessage, exception.getMessage());

        verify(redisTemplate.opsForValue()).get(REDIS_KEY);
    }

    @Test
    public void testConvertToBaseCurrency_NoConversionRequired() {
        BigDecimal amount = new BigDecimal("100");
        when(currencyApiProperties.getBaseCurrency()).thenReturn(BASE_CURRENCY);

        BigDecimal result = currencyService.convertToBaseCurrency(amount, BASE_CURRENCY);

        assertEquals(amount, result);

        verify(redisTemplate.opsForValue(), never()).get(anyString());
    }
}
