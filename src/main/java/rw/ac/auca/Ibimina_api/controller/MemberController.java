package rw.ac.auca.Ibimina_api.controller;

import rw.ac.auca.Ibimina_api.dto.MemberDtos;
import rw.ac.auca.Ibimina_api.dto.PageResponse;
import rw.ac.auca.Ibimina_api.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@PreAuthorize("hasAnyRole('ADMIN','TREASURER')")
public class MemberController {

    private final MemberService members;

    public MemberController(MemberService members) {
        this.members = members;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MemberDtos.Response create(@Valid @RequestBody MemberDtos.Request request, @AuthenticationPrincipal Jwt jwt) {
        return members.create(request, jwt.getSubject());
    }

    @PutMapping("/{id}")
    public MemberDtos.Response update(@PathVariable Long id, @Valid @RequestBody MemberDtos.Request request,
                                      @AuthenticationPrincipal Jwt jwt) {
        return members.update(id, request, jwt.getSubject());
    }

    @PatchMapping("/{id}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public MemberDtos.Response setActive(@PathVariable Long id, @RequestParam boolean value,
                                         @AuthenticationPrincipal Jwt jwt) {
        return members.setActive(id, value, jwt.getSubject());
    }

    @GetMapping
    public PageResponse<MemberDtos.Response> list(@RequestParam(required = false) Long groupId,
                                                  @PageableDefault(size = 20, sort = "fullName", direction = Sort.Direction.ASC) Pageable pageable) {
        return members.list(groupId, pageable);
    }

    @GetMapping("/{id}")
    public MemberDtos.Response get(@PathVariable Long id) {
        return members.get(id);
    }
}