package faang.school.paymentservice.controller;

import faang.school.paymentservice.config.properties.CurrencyApiProperties;
import faang.school.paymentservice.model.enums.Currency;
import faang.school.paymentservice.model.dto.PaymentRequest;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Random;

import faang.school.paymentservice.model.dto.PaymentResponse;
import faang.school.paymentservice.model.enums.PaymentStatus;
import faang.school.paymentservice.service.currency.CurrencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {
    private final CurrencyService currencyService;
    private final CurrencyApiProperties currencyApiProperties;

    @PostMapping("/payment")
    public ResponseEntity<PaymentResponse> sendPayment(@RequestBody @Validated PaymentRequest dto) {
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        String formattedSum = decimalFormat.format(dto.amount());
        int verificationCode = new Random().nextInt(1000, 10000);

        BigDecimal convertedPayment = currencyService.convertToBaseCurrency(dto.amount(), dto.currency().toString());
        String formattedConvertedPayment = decimalFormat.format(convertedPayment);
        log.info("Converted payment to {} {}", convertedPayment, currencyApiProperties.getBaseCurrency());

        String message = String.format("Dear friend! Thank you for your purchase! " +
                        "Your payment on %s %s was accepted and converted to our internal currency %s %s",
                formattedSum, dto.currency().name(), formattedConvertedPayment, currencyApiProperties.getBaseCurrency().toUpperCase());

        return ResponseEntity.ok(new PaymentResponse(
                PaymentStatus.SUCCESS,
                verificationCode,
                dto.paymentNumber(),
                convertedPayment,
                Currency.valueOf(currencyApiProperties.getBaseCurrency().toUpperCase()),
                message)
        );
    }
}
