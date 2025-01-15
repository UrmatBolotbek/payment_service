package faang.school.paymentservice.integration.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import faang.school.paymentservice.model.dto.PaymentRequest;
import faang.school.paymentservice.model.dto.currency.CurrencyRatesResponse;
import faang.school.paymentservice.model.enums.Currency;
import faang.school.paymentservice.util.BaseContextTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static faang.school.paymentservice.model.enums.Currency.EUR;
import static faang.school.paymentservice.model.enums.Currency.KGS;
import static faang.school.paymentservice.model.enums.Currency.RUB;
import static faang.school.paymentservice.model.enums.Currency.USD;
import static faang.school.paymentservice.model.enums.PaymentStatus.SUCCESS;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PaymentControllerWireMockIntegrationTest extends BaseContextTest {
    private static final String CURRENCY_RATES_REDIS_KEY = "currencyRates";
    private static final int PAYMENT_NUMBER = 1234;
    private static final Currency BASE_CURRENCY = EUR;
    private static final BigDecimal BASE_AMOUNT = BigDecimal.valueOf(20.0);
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");
    private static final String FORMATTED_AMOUNT = DECIMAL_FORMAT.format(BASE_AMOUNT);
    private static final Map<String, BigDecimal> DEFAULT_RATES_MAP = Map.of(
            EUR.toString(), BigDecimal.valueOf(1.0),
            RUB.toString(), BigDecimal.valueOf(104.99),
            USD.toString(), BigDecimal.valueOf(1.08));
    private static final int CONVERTING_BASE_SCALE = 5;

    private static WireMockServer wireMockServer;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @BeforeAll
    static void startWireMockServer() {
        wireMockServer = new WireMockServer(12123);
        wireMockServer.start();
        configureFor("127.0.0.1", wireMockServer.port());
    }

    @BeforeAll
    static void configureWireMockResponse() throws JsonProcessingException {
        LocalDateTime date = LocalDateTime.now();
        CurrencyRatesResponse mockRatesResponse = CurrencyRatesResponse.builder()
                .success(true)
                .timestamp(date.toInstant(ZoneOffset.UTC).toEpochMilli())
                .base(EUR.toString())
                .date(date.toString())
                .rates(DEFAULT_RATES_MAP)
                .build();
        String json = new ObjectMapper().writeValueAsString(mockRatesResponse);

        stubFor(WireMock.get(urlPathEqualTo("/latest"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(json)));
    }

    @AfterAll
    static void tearDown() {
        wireMockServer.stop();
    }

    @BeforeEach
    void setup() {
        redisTemplate.delete(CURRENCY_RATES_REDIS_KEY);
    }

    @Test
    void testSendPayment_SuccessBaseCurrency() throws Exception {
        int responseStatusCode = 200;
        PaymentRequest paymentRequest = new PaymentRequest(PAYMENT_NUMBER, BASE_AMOUNT, BASE_CURRENCY);
        String messageTemplate = "Dear friend! Thank you for your purchase! " +
                "Your payment on %s %s was accepted and converted to our internal currency %s %s";
        String requiredMessage = String.format(messageTemplate, FORMATTED_AMOUNT, BASE_CURRENCY, FORMATTED_AMOUNT,
                BASE_CURRENCY);

        mockMvc.perform(post("/api/v1/payment")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().is(responseStatusCode))
                .andExpect(jsonPath("$.status").value(SUCCESS.toString()))
                .andExpect(jsonPath("$.verificationCode").isNumber())
                .andExpect(jsonPath("$.paymentNumber").value(PAYMENT_NUMBER))
                .andExpect(jsonPath("$.amount").value(BASE_AMOUNT))
                .andExpect(jsonPath("$.currency").value(BASE_CURRENCY.toString()))
                .andExpect(jsonPath("$.message").value(requiredMessage));
    }

    @Test
    void testSendPayment_noConnect_currencyApi() throws Exception {
        try {
            wireMockServer.stop();

            int responseStatusCode = 400;
            PaymentRequest paymentRequest = new PaymentRequest(PAYMENT_NUMBER, BASE_AMOUNT, RUB);
            String messageTemplate = "Temporary we accept only the following currencies: %s";
            String requiredMessage = String.format(messageTemplate, BASE_CURRENCY);

            mockMvc.perform(post("/api/v1/payment")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(paymentRequest)))
                    .andExpect(status().is(responseStatusCode))
                    .andExpect(jsonPath("$.message").value(requiredMessage));
        } finally {
            wireMockServer.start();
        }
    }

    @Test
    void testSendPayment_SuccessNoBaseCurrency() throws Exception {
        Currency requestCurrency = USD;
        BigDecimal rate = DEFAULT_RATES_MAP.get(requestCurrency.toString());
        BigDecimal requiredAmount = BASE_AMOUNT.divide(rate, CONVERTING_BASE_SCALE, RoundingMode.HALF_UP);
        String formattedRequiredAmount = DECIMAL_FORMAT.format(requiredAmount);
        int responseStatusCode = 200;
        PaymentRequest paymentRequest = new PaymentRequest(PAYMENT_NUMBER, BASE_AMOUNT, requestCurrency);
        String messageTemplate = "Dear friend! Thank you for your purchase! " +
                "Your payment on %s %s was accepted and converted to our internal currency %s %s";
        String requiredMessage = String.format(messageTemplate, FORMATTED_AMOUNT, requestCurrency, formattedRequiredAmount,
                BASE_CURRENCY);

        mockMvc.perform(post("/api/v1/payment")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().is(responseStatusCode))
                .andExpect(jsonPath("$.status").value(SUCCESS.toString()))
                .andExpect(jsonPath("$.verificationCode").isNumber())
                .andExpect(jsonPath("$.paymentNumber").value(PAYMENT_NUMBER))
                .andExpect(jsonPath("$.amount").value(requiredAmount))
                .andExpect(jsonPath("$.currency").value(BASE_CURRENCY.toString()))
                .andExpect(jsonPath("$.message").value(requiredMessage));
    }

    @Test
    void testSendPayment_unAvailableCurrency() throws Exception {
        Currency currency = KGS;
        int responseStatusCode = 400;
        PaymentRequest paymentRequest = new PaymentRequest(PAYMENT_NUMBER, BASE_AMOUNT, currency);
        String messageTemplate =
                "Exchange rate for currency '%s' is not available. Available only following currencies: %s";
        List<String> availableCurrencies = DEFAULT_RATES_MAP.keySet().stream()
                .sorted()
                .toList();
        String requiredMessage = String.format(messageTemplate, currency, availableCurrencies);

        mockMvc.perform(post("/api/v1/payment")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().is(responseStatusCode))
                .andExpect(jsonPath("$.message").value(requiredMessage));
    }
}
