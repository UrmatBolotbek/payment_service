package faang.school.paymentservice.config.web_client;

import org.springframework.web.reactive.function.client.WebClient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl("https://api.exchangeratesapi.io/v1/")
                .build();
    }

}
