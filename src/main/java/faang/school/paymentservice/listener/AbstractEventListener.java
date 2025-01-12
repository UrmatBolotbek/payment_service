package faang.school.paymentservice.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.Topic;

@Slf4j
@AllArgsConstructor
public abstract class AbstractEventListener<T> implements EventMessageListener {
    private final ObjectMapper objectMapper;
    @Getter
    private final Topic topic;

    public abstract void handleMessage(T event);

    public abstract Class<T> getEventType();

    public abstract void handleException(Exception exception);

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            T event = objectMapper.readValue(message.getBody(), getEventType());
            handleMessage(event);
        } catch (Exception exception) {
            handleException(exception);
        }
    }
}
