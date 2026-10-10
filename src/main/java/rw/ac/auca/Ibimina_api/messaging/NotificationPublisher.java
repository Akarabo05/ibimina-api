package rw.ac.auca.Ibimina_api.messaging;

import rw.ac.auca.Ibimina_api.entity.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationPublisher {

    private static final Logger log = LoggerFactory.getLogger(NotificationPublisher.class);
    private final RabbitTemplate rabbit;

    public NotificationPublisher(RabbitTemplate rabbit) {
        this.rabbit = rabbit;
    }

    /** SMS always (every member has a phone); email only when the member gave an address. */
    public void notifyMember(Member member, String message) {
        if (member.getEmail() != null && !member.getEmail().isBlank()) {
            send(RabbitConfig.EMAIL_KEY, new NotificationEvent("EMAIL", member.getFullName(), member.getEmail(), message));
        }
        send(RabbitConfig.SMS_KEY, new NotificationEvent("SMS", member.getFullName(), member.getPhoneNumber(), message));
    }

    private void send(String routingKey, NotificationEvent event) {
        try {
            rabbit.convertAndSend(RabbitConfig.EXCHANGE, routingKey, event);
        } catch (AmqpException ex) {
            // A broker outage must not make a saved contribution look like a failed request.
            log.warn("Could not publish {} notification: {}", event.channel(), ex.getMessage());
        }
    }
}