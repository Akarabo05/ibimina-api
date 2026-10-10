package rw.ac.auca.Ibimina_api.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/** Exceptions are caught on purpose: rethrowing would make RabbitMQ redeliver the message forever. */
@Component
public class NotificationListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationListener.class);

    private final ObjectProvider<JavaMailSender> mailSender;
    private final NotificationLogRepository logs;
    private final String from;

    public NotificationListener(ObjectProvider<JavaMailSender> mailSender,
                                NotificationLogRepository logs,
                                @Value("${app.notification.from}") String from) {
        this.mailSender = mailSender;
        this.logs = logs;
        this.from = from;
    }

    @RabbitListener(queues = RabbitConfig.EMAIL_QUEUE)
    public void onEmail(NotificationEvent event) {
        try {
            JavaMailSender sender = mailSender.getIfAvailable();
            if (sender == null) throw new IllegalStateException("Mail is not configured");
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom(from);
            mail.setTo(event.recipient());
            mail.setSubject("Ibimina notification");
            mail.setText("Hello " + event.recipientName() + ",\n\n" + event.message() + "\n\nIbimina Tracking System");
            sender.send(mail);
            record(event, "SENT", null);
        } catch (Exception ex) {
            log.warn("Email to {} failed: {}", event.recipient(), ex.getMessage());
            record(event, "FAILED", ex.getMessage());
        }
    }

    /** SMS gateway stub: logs the message. Replace the log line with a real provider call. */
    @RabbitListener(queues = RabbitConfig.SMS_QUEUE)
    public void onSms(NotificationEvent event) {
        try {
            log.info("[SMS -> {}] {}", event.recipient(), event.message());
            record(event, "SENT", null);
        } catch (Exception ex) {
            record(event, "FAILED", ex.getMessage());
        }
    }

    private void record(NotificationEvent event, String status, String error) {
        try {
            logs.save(new NotificationLog(event.channel(), event.recipient(), event.message(), status, error));
        } catch (Exception ex) {
            log.error("Could not store notification log", ex);
        }
    }
}