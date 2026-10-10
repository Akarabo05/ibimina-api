package rw.ac.auca.Ibimina_api.controller;

import rw.ac.auca.Ibimina_api.audit.AuditLog;
import rw.ac.auca.Ibimina_api.audit.AuditLogRepository;
import rw.ac.auca.Ibimina_api.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit-logs")
@PreAuthorize("hasRole('ADMIN')")
public class AuditController {

    private final AuditLogRepository repository;

    public AuditController(AuditLogRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public PageResponse<AuditLog> list(@RequestParam(required = false) String entityType,
                                       @PageableDefault(size = 25, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<AuditLog> page = entityType == null ? repository.findAll(pageable) : repository.findByEntityType(entityType, pageable);
        return PageResponse.of(page);
    }
}