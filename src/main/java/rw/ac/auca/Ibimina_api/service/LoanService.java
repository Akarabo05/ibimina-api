package rw.ac.auca.Ibimina_api.service;

import rw.ac.auca.Ibimina_api.audit.AuditService;
import rw.ac.auca.Ibimina_api.dto.LoanDtos;
import rw.ac.auca.Ibimina_api.dto.PageResponse;
import rw.ac.auca.Ibimina_api.entity.ContributionStatus;
import rw.ac.auca.Ibimina_api.entity.Loan;
import rw.ac.auca.Ibimina_api.entity.LoanRepayment;
import rw.ac.auca.Ibimina_api.entity.LoanStatus;
import rw.ac.auca.Ibimina_api.entity.Member;
import rw.ac.auca.Ibimina_api.exception.ResourceNotFoundException;
import rw.ac.auca.Ibimina_api.messaging.NotificationPublisher;
import rw.ac.auca.Ibimina_api.repository.ContributionRepository;
import rw.ac.auca.Ibimina_api.repository.LoanRepaymentRepository;
import rw.ac.auca.Ibimina_api.repository.LoanRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LoanService {

    /** A member may borrow at most this many times their own active savings. */
    static final BigDecimal MAX_SAVINGS_MULTIPLIER = new BigDecimal("3");

    private final LoanRepository loans;
    private final LoanRepaymentRepository repayments;
    private final ContributionRepository contributions;
    private final MemberService memberService;
    private final AuditService audit;
    private final NotificationPublisher notifications;

    public LoanService(LoanRepository loans, LoanRepaymentRepository repayments, ContributionRepository contributions,
                       MemberService memberService, AuditService audit, NotificationPublisher notifications) {
        this.loans = loans;
        this.repayments = repayments;
        this.contributions = contributions;
        this.memberService = memberService;
        this.audit = audit;
        this.notifications = notifications;
    }

    @Transactional
    public LoanDtos.Response request(LoanDtos.Request r, String actor) {
        Member member = memberService.find(r.memberId());
        if (!member.isActive()) {
            throw new IllegalArgumentException("Member " + member.getFullName() + " is not active");
        }
        if (!r.dueDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Due date must be in the future");
        }
        if (loans.existsByMemberIdAndStatusIn(member.getId(), List.of(LoanStatus.REQUESTED, LoanStatus.APPROVED))) {
            throw new IllegalStateException("This member already has an open loan");
        }
        BigDecimal savings = orZero(contributions.sumByMember(member.getId(), ContributionStatus.ACTIVE));
        if (r.amount().compareTo(savings.multiply(MAX_SAVINGS_MULTIPLIER)) > 0) {
            throw new IllegalArgumentException("Loan cannot exceed " + MAX_SAVINGS_MULTIPLIER
                    + " times the member's savings (savings: " + savings + " RWF)");
        }
        Loan loan = new Loan();
        loan.setMember(member);
        loan.setPrincipal(r.amount());
        loan.setInterestRate(r.interestRate());
        loan.setTotalDue(totalDue(r.amount(), r.interestRate()));
        loan.setDueDate(r.dueDate());
        loan.setRequestedBy(actor);
        loan = loans.save(loan);

        audit.log(actor, "REQUEST_LOAN", "Loan", String.valueOf(loan.getId()),
                member.getFullName() + " requested " + r.amount() + " RWF");
        return toResponse(loan);
    }

    @Transactional
    public LoanDtos.Response approve(Long id, String actor) {
        Loan loan = decide(id, LoanStatus.APPROVED, actor);
        notifications.notifyMember(loan.getMember(), "Your loan of " + loan.getPrincipal()
                + " RWF was approved. Total to repay: " + loan.getTotalDue() + " RWF by " + loan.getDueDate() + ".");
        return toResponse(loan);
    }

    @Transactional
    public LoanDtos.Response reject(Long id, String actor) {
        Loan loan = decide(id, LoanStatus.REJECTED, actor);
        notifications.notifyMember(loan.getMember(), "Your loan request of " + loan.getPrincipal() + " RWF was not approved.");
        return toResponse(loan);
    }

    @Transactional
    public LoanDtos.Response repay(Long id, BigDecimal amount, String actor) {
        Loan loan = find(id);
        if (loan.getStatus() != LoanStatus.APPROVED) {
            throw new IllegalStateException("Only approved loans can be repaid (status: " + loan.getStatus() + ")");
        }
        if (amount.compareTo(loan.getOutstanding()) > 0) {
            throw new IllegalArgumentException("Payment exceeds the outstanding balance of " + loan.getOutstanding() + " RWF");
        }
        repayments.save(new LoanRepayment(loan, amount, actor));
        loan.setAmountPaid(loan.getAmountPaid().add(amount));
        if (loan.getOutstanding().signum() == 0) {
            loan.setStatus(LoanStatus.REPAID);
        }
        audit.log(actor, "LOAN_REPAYMENT", "Loan", String.valueOf(id), amount + " RWF paid, outstanding " + loan.getOutstanding());
        notifications.notifyMember(loan.getMember(), "We received your loan repayment of " + amount
                + " RWF. Outstanding balance: " + loan.getOutstanding() + " RWF.");
        return toResponse(loan);
    }

    @Transactional(readOnly = true)
    public PageResponse<LoanDtos.Response> list(Long memberId, Pageable pageable) {
        Page<Loan> page = memberId == null ? loans.findAll(pageable) : loans.findByMemberId(memberId, pageable);
        return PageResponse.of(page.map(LoanService::toResponse));
    }

    @Transactional(readOnly = true)
    public List<LoanDtos.RepaymentResponse> repaymentsOf(Long loanId) {
        find(loanId);
        return repayments.findByLoanIdOrderByPaidAtDesc(loanId).stream()
                .map(p -> new LoanDtos.RepaymentResponse(p.getId(), p.getAmount(), p.getPaidAt(), p.getRecordedBy()))
                .toList();
    }

    private Loan decide(Long id, LoanStatus newStatus, String actor) {
        Loan loan = find(id);
        if (loan.getStatus() != LoanStatus.REQUESTED) {
            throw new IllegalStateException("Loan was already " + loan.getStatus());
        }
        loan.setStatus(newStatus);
        loan.setDecidedBy(actor);
        loan.setDecidedAt(LocalDateTime.now());
        audit.log(actor, "LOAN_" + newStatus.name(), "Loan", String.valueOf(id), "Loan " + newStatus.name().toLowerCase());
        return loan;
    }

    private Loan find(Long id) {
        return loans.findById(id).orElseThrow(() -> new ResourceNotFoundException("Loan " + id + " not found"));
    }

    static BigDecimal totalDue(BigDecimal principal, BigDecimal ratePercent) {
        BigDecimal factor = BigDecimal.ONE.add(ratePercent.divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP));
        return principal.multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal orZero(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    static LoanDtos.Response toResponse(Loan l) {
        Member m = l.getMember();
        return new LoanDtos.Response(l.getId(), m.getId(), m.getFullName(), m.getGroup().getGroupName(),
                l.getPrincipal(), l.getInterestRate(), l.getTotalDue(), l.getAmountPaid(), l.getOutstanding(),
                l.getDueDate(), l.getStatus().name(), l.getRequestedBy(), l.getRequestedAt(),
                l.getDecidedBy(), l.getDecidedAt());
    }
}