package rw.ac.auca.Ibimina_api.audit;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "audit_logs")
public class AuditLog {

    @Id
    private String id;
    @Indexed
    private Instant timestamp = Instant.now();
    private String actor;
    private String action;
    @Indexed
    private String entityType;
    private String entityId;
    private String details;

    public AuditLog() {}

    public AuditLog(String actor, String action, String entityType, String entityId, String details) {
        this.actor = actor;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.details = details;
    }

    public String getId() { return id; }
    public Instant getTimestamp() { return timestamp; }
    public String getActor() { return actor; }
    public String getAction() { return action; }
    public String getEntityType() { return entityType; }
    public String getEntityId() { return entityId; }
    public String getDetails() { return details; }
}