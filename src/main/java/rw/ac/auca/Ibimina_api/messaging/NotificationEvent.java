package rw.ac.auca.Ibimina_api.messaging;

import java.io.Serializable;

public record NotificationEvent(String channel, String recipientName, String recipient, String message)
        implements Serializable {
}