package faang.school.paymentservice.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.paymentservice.model.dto.operation.CheckingPaymentStatusAndBalance;
import faang.school.paymentservice.service.operation.CheckingBalanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AuthPaymentResponseListener extends AbstractEventListener<CheckingPaymentStatusAndBalance> {
    private final CheckingBalanceService checkingBalanceService;

    public AuthPaymentResponseListener(ObjectMapper objectMapper,
                                       @Value("${spring.data.redis.channels.auth-payment.response}") String topic,
                                       CheckingBalanceService checkingBalanceService) {
        super(objectMapper, new ChannelTopic(topic));
        this.checkingBalanceService = checkingBalanceService;
    }

    @Override
    public void handleMessage(CheckingPaymentStatusAndBalance event) {
        checkingBalanceService.checkBalance(event, event.getStatus());
    }

    @Override
    public Class<CheckingPaymentStatusAndBalance> getEventType() {
        return CheckingPaymentStatusAndBalance.class;
    }

    @Override
    public void handleException(Exception exception) {
        log.error("Error processing auth-payment-response event", exception);
    }
}
