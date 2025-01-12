package faang.school.paymentservice.publisher;

public interface EventPublisher<T> {
    void publish(T event);
}