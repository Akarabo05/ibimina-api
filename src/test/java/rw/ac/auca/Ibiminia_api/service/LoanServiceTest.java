package rw.ac.auca.Ibimina_api.service;

import rw.ac.auca.Ibimina_api.audit.AuditService;
import rw.ac.auca.Ibimina_api.dto.LoanDtos;
import rw.ac.auca.Ibimina_api.entity.*;
import rw.ac.auca.Ibimina_api.messaging.NotificationPublisher;
import rw.ac.auca.Ibimina_api.repository.ContributionRepository;
import rw.ac.auca.Ibimina_api.repository.LoanRepaymentRepository;
import rw.ac.auca.Ibimina_api.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock LoanRepository loans;
    @Mock LoanRepaymentRepository repayments;
    @Mock ContributionRepository contributions;
    @Mock MemberService memberService;
    @Mock AuditService audit;
    @Mock NotificationPublisher notifications;

    LoanService service;
    Member member;

    @BeforeEach
    void setUp() {
        service = new LoanService(loans, repayments, contributions, memberService, audit, notifications);
        member = new Member("Uwase Alice", "0788000001", null, new Group("Twisungane", "MONTHLY", new BigDecimal("5000.00")));
        member.setId(1L);
    }

    @Test
    void totalDueAddsInterest() {
        assertEquals(new BigDecimal("11000.00"), LoanService.totalDue(new BigDecimal("10000"), new BigDecimal("10")));
    }

    @Test
    void loanAboveThreeTimesSavingsIsRejected() {
        when(memberService.find(1L)).thenReturn(member);
        when(loans.existsByMemberIdAndStatusIn(eq(1L), anyCollection())).thenReturn(false);
        when(contributions.sumByMember(1L, ContributionStatus.ACTIVE)).thenReturn(new BigDecimal("10000.00"));

        var request = new LoanDtos.Request(1L, new BigDecimal("30001.00"), new BigDecimal("5"), LocalDate.now().plusMonths(3));
        assertThrows(IllegalArgumentException.class, () -> service.request(request, "treasurer"));
        verify(loans, never()).save(any());
    }

    @Test
    void memberWithOpenLoanCannotRequestAnother() {
        when(memberService.find(1L)).thenReturn(member);
        when(loans.existsByMemberIdAndStatusIn(eq(1L), anyCollection())).thenReturn(true);

        var request = new LoanDtos.Request(1L, new BigDecimal("5000.00"), new BigDecimal("5"), LocalDate.now().plusMonths(3));
        assertThrows(IllegalStateException.class, () -> service.request(request, "treasurer"));
    }

    @Test
    void finalRepaymentMarksLoanRepaid() {
        Loan loan = new Loan();
        loan.setMember(member);
        loan.setPrincipal(new BigDecimal("10000.00"));
        loan.setInterestRate(BigDecimal.ZERO);
        loan.setTotalDue(new BigDecimal("10000.00"));
        loan.setDueDate(LocalDate.now().plusMonths(1));
        loan.setStatus(LoanStatus.APPROVED);
        when(loans.findById(5L)).thenReturn(Optional.of(loan));

        var response = service.repay(5L, new BigDecimal("10000.00"), "treasurer");

        assertEquals("REPAID", response.status());
        assertEquals(0, response.outstanding().signum());
    }

    @Test
    void overpaymentIsRejected() {
        Loan loan = new Loan();
        loan.setMember(member);
        loan.setTotalDue(new BigDecimal("1000.00"));
        loan.setStatus(LoanStatus.APPROVED);
        when(loans.findById(5L)).thenReturn(Optional.of(loan));

        assertThrows(IllegalArgumentException.class, () -> service.repay(5L, new BigDecimal("1500.00"), "treasurer"));
    }

    @Test
    void pendingLoanCannotBeRepaid() {
        Loan loan = new Loan();
        loan.setMember(member);
        loan.setTotalDue(new BigDecimal("1000.00"));
        when(loans.findById(5L)).thenReturn(Optional.of(loan));

        assertThrows(IllegalStateException.class, () -> service.repay(5L, new BigDecimal("100.00"), "treasurer"));
    }
}