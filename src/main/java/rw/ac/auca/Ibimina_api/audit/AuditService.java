package rw.ac.auca.Ibimina_api.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);
    private final AuditLogRepository repository;

    public AuditService(AuditLogRepository repository) {
        this.repository = repository;
    }

    /** An audit failure must not break the business operation, but is always logged loudly. */
    public void log(String actor, String action, String entityType, String entityId, String details) {
        try {
            repository.save(new AuditLog(actor, action, entityType, entityId, details));
        } catch (Exception ex) {
            log.error("AUDIT WRITE FAILED actor={} action={} {}#{}", actor, action, entityType, entityId, ex);
        }
    }
}