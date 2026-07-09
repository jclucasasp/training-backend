package org.lucas.arbackend.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EMAIL_QUEUE = "email-queue";
    public static final String EMAIL_EXCHANGE = "email-exchange";
    public static final String EMAIL_ROUTING_KEY = "email-routing-key";

    private static final String PAYFAST_ITN_QUEUE = "payfast-itn-queue";
    private static final String PAYFAST_ITN_EXCHANGE = "payfast-itn-exchange";
    private static final String PAYFAST_ITN_ROUTING_KEY = "payfast-itn-routing-key";


    @Bean
    public Queue emailQueue() {
        return new Queue(EMAIL_QUEUE, true);
    }

    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(EMAIL_EXCHANGE, true, false);
    }

    @Bean
    public Binding emailBinding(Queue emailQueue, DirectExchange emailExchange) {
        return BindingBuilder.
                bind(emailQueue)
                .to(emailExchange)
                .with(EMAIL_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Queue payfastItnQueue() {
        return new Queue(PAYFAST_ITN_QUEUE, true);
    }

    @Bean
    public DirectExchange payfastItnExchange() {
        return new DirectExchange(PAYFAST_ITN_EXCHANGE, true, false);
    }

    @Bean
    public Binding payfastItnBinding(Queue payfastItnQueue, DirectExchange payfastItnExchange) {
        return BindingBuilder.
                bind(payfastItnQueue)
                .to(payfastItnExchange)
                .with(PAYFAST_ITN_ROUTING_KEY);
    }
}
