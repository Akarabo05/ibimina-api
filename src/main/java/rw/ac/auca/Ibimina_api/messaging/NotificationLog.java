package rw.ac.auca.Ibimina_api.messaging;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "notification_logs")
public class NotificationLog {

    @Id
    private String id;
    private String channel;
    private String recipient;
    private String message;
    private String status;
    private String error;
    private Instant createdAt = Instant.now();

    public NotificationLog() {}

    public NotificationLog(String channel, String recipient, String message, String status, String error) {
        this.channel = channel;
        this.recipient = recipient;
        this.message = message;
        this.status = status;
        this.error = error;
    }

    public String getId() { return id; }
    public String getChannel() { return channel; }
    public String getRecipient() { return recipient; }
    public String getMessage() { return message; }
    public String getStatus() { return status; }
    public String getError() { return error; }
    public Instant getCreatedAt() { return createdAt; }
}