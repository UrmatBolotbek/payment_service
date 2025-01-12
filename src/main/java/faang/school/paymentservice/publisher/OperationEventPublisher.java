package faang.school.paymentservice.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.paymentservice.model.dto.operation.OperationMessage;
import faang.school.paymentservice.model.enums.OperationStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class OperationEventPublisher implements EventPublisher<OperationMessage> {
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final Map<OperationStatus, String> channelMap;

    public OperationEventPublisher(ObjectMapper objectMapper,
                                   RedisTemplate<String, Object> redisTemplate,
                                   @Value("${spring.data.redis.channels.auth-payment.request}") String authChannel,
                                   @Value("${spring.data.redis.channels.cancel-payment.request}") String cancelChannel,
                                   @Value("${spring.data.redis.channels.clearing-payment.request}") String clearingChannel,
                                   @Value("${spring.data.redis.channels.error-payment.request}") String errorChannel) {
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
        this.channelMap = Map.of(
                OperationStatus.PENDING, authChannel,
                OperationStatus.CANCELLATION, cancelChannel,
                OperationStatus.CLEARING, clearingChannel,
                OperationStatus.ERROR, errorChannel
        );
    }

    @Override
    public void publish(OperationMessage event) {
        String channel = channelMap.get(event.getStatus());
        if (channel == null) {
            throw new IllegalArgumentException("No Redis channel configured for status: " + event.getStatus());
        }

        try {
            String json = objectMapper.writeValueAsString(event);
            redisTemplate.convertAndSend(channel, json);
            log.info("Event published to Redis channel {}: {}", channel, json);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize event", ex);
        }
    }
}
