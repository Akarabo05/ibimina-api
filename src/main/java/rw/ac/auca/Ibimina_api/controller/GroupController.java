package rw.ac.auca.Ibimina_api.controller;

import rw.ac.auca.Ibimina_api.dto.GroupDtos;
import rw.ac.auca.Ibimina_api.dto.ReportDtos;
import rw.ac.auca.Ibimina_api.service.GroupService;
import rw.ac.auca.Ibimina_api.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groups;
    private final ReportService reports;

    public GroupController(GroupService groups, ReportService reports) {
        this.groups = groups;
        this.reports = reports;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public GroupDtos.Response create(@Valid @RequestBody GroupDtos.Request request, @AuthenticationPrincipal Jwt jwt) {
        return groups.create(request, jwt.getSubject());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public GroupDtos.Response update(@PathVariable Long id, @Valid @RequestBody GroupDtos.Request request,
                                     @AuthenticationPrincipal Jwt jwt) {
        return groups.update(id, request, jwt.getSubject());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TREASURER')")
    public List<GroupDtos.Response> list() {
        return groups.list();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TREASURER')")
    public GroupDtos.Response get(@PathVariable Long id) {
        return groups.get(id);
    }

    /** Monthly report: expected vs collected and who has not paid. month format: 2026-10 */
    @GetMapping("/{id}/summary")
    @PreAuthorize("hasAnyRole('ADMIN','TREASURER')")
    public ReportDtos.GroupSummary summary(@PathVariable Long id,
                                           @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return reports.groupSummary(id, month == null ? YearMonth.now() : month);
    }
}