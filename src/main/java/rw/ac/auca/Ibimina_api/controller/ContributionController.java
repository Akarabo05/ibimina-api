package rw.ac.auca.Ibimina_api.controller;

import rw.ac.auca.Ibimina_api.dto.ContributionDtos;
import rw.ac.auca.Ibimina_api.dto.PageResponse;
import rw.ac.auca.Ibimina_api.security.CurrentUser;
import rw.ac.auca.Ibimina_api.service.ContributionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/**
 * Separation of duties: the treasurer RECORDS, only an admin can REVERSE, nobody can edit or delete.
 * There are intentionally no PUT/DELETE endpoints.
 */
@RestController
@RequestMapping("/api")
public class ContributionController {

    private final ContributionService contributions;

    public ContributionController(ContributionService contributions) {
        this.contributions = contributions;
    }

    @PostMapping("/contributions")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','TREASURER')")
    public ContributionDtos.Response record(@Valid @RequestBody ContributionDtos.Request request,
                                            @AuthenticationPrincipal Jwt jwt) {
        return contributions.record(request, jwt.getSubject());
    }

    @PostMapping("/contributions/{id}/reverse")
    @PreAuthorize("hasRole('ADMIN')")
    public ContributionDtos.Response reverse(@PathVariable Long id, @Valid @RequestBody ContributionDtos.ReverseRequest request,
                                             @AuthenticationPrincipal Jwt jwt) {
        return contributions.reverse(id, request.reason(), jwt.getSubject());
    }

    @GetMapping("/contributions")
    @PreAuthorize("hasAnyRole('ADMIN','TREASURER')")
    public PageResponse<ContributionDtos.Response> list(@RequestParam(required = false) Long memberId,
                                                        @PageableDefault(size = 20, sort = "recordedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return contributions.list(memberId, pageable);
    }

    @GetMapping("/me/contributions")
    @PreAuthorize("hasRole('MEMBER')")
    public PageResponse<ContributionDtos.Response> mine(@AuthenticationPrincipal Jwt jwt,
                                                        @PageableDefault(size = 20, sort = "recordedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return contributions.list(CurrentUser.requireMemberId(jwt), pageable);
    }
}