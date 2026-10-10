package rw.ac.auca.Ibimina_api.service;

import rw.ac.auca.Ibimina_api.audit.AuditService;
import rw.ac.auca.Ibimina_api.dto.ContributionDtos;
import rw.ac.auca.Ibimina_api.entity.*;
import rw.ac.auca.Ibimina_api.exception.ResourceNotFoundException;
import rw.ac.auca.Ibimina_api.messaging.NotificationPublisher;
import rw.ac.auca.Ibimina_api.repository.ContributionRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContributionServiceTest {

    @Mock ContributionRepository contributions;
    @Mock MemberService memberService;
    @Mock AuditService audit;
    @Mock NotificationPublisher notifications;

    ContributionService service;
    Member member;

    @BeforeEach
    void setUp() {
        service = new ContributionService(contributions, memberService, audit, notifications);
        Group group = new Group("Twisungane", "MONTHLY", new BigDecimal("5000.00"));
        member = new Member("Uwase Alice", "0788000001", null, group);
        member.setId(1L);
    }

    @Test
    void recordSavesContributionWithReceiptAuditAndNotification() {
        when(memberService.find(1L)).thenReturn(member);
        when(contributions.save(any(Contribution.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = service.record(new ContributionDtos.Request(1L, new BigDecimal("5000.00"), LocalDate.now()), "treasurer");

        assertTrue(response.receiptNumber().startsWith("RCT-"));
        assertEquals("ACTIVE", response.status());
        assertEquals("treasurer", response.recordedBy());
        verify(audit).log(eq("treasurer"), eq("RECORD_CONTRIBUTION"), eq("Contribution"), any(), any());
        verify(notifications).notifyMember(eq(member), any());
    }

    @Test
    void recordRejectsInactiveMember() {
        member.setActive(false);
        when(memberService.find(1L)).thenReturn(member);

        assertThrows(IllegalArgumentException.class, () ->
                service.record(new ContributionDtos.Request(1L, new BigDecimal("5000.00"), LocalDate.now()), "treasurer"));
        verify(contributions, never()).save(any());
    }

    @Test
    void recordRejectsFutureDate() {
        when(memberService.find(1L)).thenReturn(member);

        assertThrows(IllegalArgumentException.class, () ->
                service.record(new ContributionDtos.Request(1L, new BigDecimal("5000.00"), LocalDate.now().plusDays(1)), "treasurer"));
    }

    @Test
    void reverseMarksContributionAndCannotBeDoneTwice() {
        Contribution c = new Contribution(new BigDecimal("5000.00"), LocalDate.now(), member);
        c.setReceiptNumber("RCT-1");
        when(contributions.findById(9L)).thenReturn(Optional.of(c));

        var response = service.reverse(9L, "Entered the wrong member", "admin");

        assertEquals("REVERSED", response.status());
        assertEquals("admin", response.reversedBy());
        assertThrows(IllegalStateException.class, () -> service.reverse(9L, "Second attempt here", "admin"));
    }

    @Test
    void reverseUnknownContributionIsNotFound() {
        when(contributions.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.reverse(99L, "Does not exist", "admin"));
    }
}