package org.lucas.arbackend.config;

import com.rabbitmq.client.AMQP;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EMAIL_QUEUE = "email-queue";
    public static final String EMAIL_EXCHANGE = "email-exchange";
    public static final String EMAIL_ROUTING_KEY = "email-routing-key";

    public static final String PAYFAST_ITN_QUEUE = "payfast-itn-queue";
    public static final String PAYFAST_ITN_EXCHANGE = "payfast-itn-exchange";
    public static final String PAYFAST_ITN_ROUTING_KEY = "payfast-itn-routing-key";

    @Bean
    public Queue emailQueue() {
        return new Queue(EMAIL_QUEUE, true);
    }

    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(EMAIL_EXCHANGE, true, false);
    }

    // 2. Explicitly specify qualifiers to avoid dependency injection collisions
    @Bean
    public Binding emailBinding(
            @Qualifier("emailQueue") Queue emailQueue,
            @Qualifier("directExchange") DirectExchange directExchange) {
        return BindingBuilder
                .bind(emailQueue)
                .to(directExchange)
                .with(EMAIL_ROUTING_KEY);
    }

    @Bean
    public Queue payfastItnQueue() {
        return new Queue(PAYFAST_ITN_QUEUE, true);
    }

    @Bean
    public DirectExchange payfastItnExchange() {
        return new DirectExchange(PAYFAST_ITN_EXCHANGE, true, false);
    }

    // 3. Do the same explicit mapping here for the PayFast pipeline
    @Bean
    public Binding payfastItnBinding(
            @Qualifier("payfastItnQueue") Queue payfastItnQueue,
            @Qualifier("payfastItnExchange") DirectExchange payfastItnExchange) {
        return BindingBuilder
                .bind(payfastItnQueue)
                .to(payfastItnExchange)
                .with(PAYFAST_ITN_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
