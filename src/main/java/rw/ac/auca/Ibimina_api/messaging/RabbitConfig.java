package rw.ac.auca.Ibimina_api.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "ibimina.events";
    public static final String EMAIL_QUEUE = "ibimina.notifications.email";
    public static final String SMS_QUEUE = "ibimina.notifications.sms";
    public static final String EMAIL_KEY = "notify.email";
    public static final String SMS_KEY = "notify.sms";

    @Bean
    TopicExchange eventsExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    Queue emailQueue() {
        return new Queue(EMAIL_QUEUE, true);
    }

    @Bean
    Queue smsQueue() {
        return new Queue(SMS_QUEUE, true);
    }

    @Bean
    Binding emailBinding(Queue emailQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(emailQueue).to(eventsExchange).with(EMAIL_KEY);
    }

    @Bean
    Binding smsBinding(Queue smsQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(smsQueue).to(eventsExchange).with(SMS_KEY);
    }

    /** Only our own event class (and JDK types) may be deserialised from the broker. */
    @Bean
    MessageConverter messageConverter() {
        SimpleMessageConverter converter = new SimpleMessageConverter();
        converter.setAllowedListPatterns(List.of("rw.ac.auca.Ibimina_api.*", "java.*"));
        return converter;
    }
}