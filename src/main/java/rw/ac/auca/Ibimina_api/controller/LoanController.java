package rw.ac.auca.Ibimina_api.controller;

import rw.ac.auca.Ibimina_api.dto.LoanDtos;
import rw.ac.auca.Ibimina_api.dto.PageResponse;
import rw.ac.auca.Ibimina_api.security.CurrentUser;
import rw.ac.auca.Ibimina_api.service.LoanService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class LoanController {

    private final LoanService loans;

    public LoanController(LoanService loans) {
        this.loans = loans;
    }

    @PostMapping("/loans")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','TREASURER')")
    public LoanDtos.Response request(@Valid @RequestBody LoanDtos.Request request, @AuthenticationPrincipal Jwt jwt) {
        return loans.request(request, jwt.getSubject());
    }

    @PostMapping("/loans/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public LoanDtos.Response approve(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return loans.approve(id, jwt.getSubject());
    }

    @PostMapping("/loans/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public LoanDtos.Response reject(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return loans.reject(id, jwt.getSubject());
    }

    @PostMapping("/loans/{id}/repayments")
    @PreAuthorize("hasAnyRole('ADMIN','TREASURER')")
    public LoanDtos.Response repay(@PathVariable Long id, @Valid @RequestBody LoanDtos.RepaymentRequest request,
                                   @AuthenticationPrincipal Jwt jwt) {
        return loans.repay(id, request.amount(), jwt.getSubject());
    }

    @GetMapping("/loans/{id}/repayments")
    @PreAuthorize("hasAnyRole('ADMIN','TREASURER')")
    public List<LoanDtos.RepaymentResponse> repayments(@PathVariable Long id) {
        return loans.repaymentsOf(id);
    }

    @GetMapping("/loans")
    @PreAuthorize("hasAnyRole('ADMIN','TREASURER')")
    public PageResponse<LoanDtos.Response> list(@RequestParam(required = false) Long memberId,
                                                @PageableDefault(size = 20, sort = "requestedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return loans.list(memberId, pageable);
    }

    @GetMapping("/me/loans")
    @PreAuthorize("hasRole('MEMBER')")
    public PageResponse<LoanDtos.Response> mine(@AuthenticationPrincipal Jwt jwt,
                                                @PageableDefault(size = 20, sort = "requestedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return loans.list(CurrentUser.requireMemberId(jwt), pageable);
    }
}