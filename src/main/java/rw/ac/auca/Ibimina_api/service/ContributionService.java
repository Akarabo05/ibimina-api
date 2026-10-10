package rw.ac.auca.Ibimina_api.service;

import rw.ac.auca.Ibimina_api.audit.AuditService;
import rw.ac.auca.Ibimina_api.dto.ContributionDtos;
import rw.ac.auca.Ibimina_api.dto.PageResponse;
import rw.ac.auca.Ibimina_api.entity.Contribution;
import rw.ac.auca.Ibimina_api.entity.ContributionStatus;
import rw.ac.auca.Ibimina_api.entity.Member;
import rw.ac.auca.Ibimina_api.exception.ResourceNotFoundException;
import rw.ac.auca.Ibimina_api.messaging.NotificationPublisher;
import rw.ac.auca.Ibimina_api.repository.ContributionRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class ContributionService {

    private final ContributionRepository contributions;
    private final MemberService memberService;
    private final AuditService audit;
    private final NotificationPublisher notifications;

    public ContributionService(ContributionRepository contributions, MemberService memberService,
                               AuditService audit, NotificationPublisher notifications) {
        this.contributions = contributions;
        this.memberService = memberService;
        this.audit = audit;
        this.notifications = notifications;
    }

    @Transactional
    @CacheEvict(cacheNames = "groupSummary", allEntries = true)
    public ContributionDtos.Response record(ContributionDtos.Request r, String actor) {
        Member member = memberService.find(r.memberId());
        if (!member.isActive()) {
            throw new IllegalArgumentException("Member " + member.getFullName() + " is not active");
        }
        if (r.contributionDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Contribution date cannot be in the future");
        }
        Contribution c = new Contribution(r.amount(), r.contributionDate(), member);
        c.setRecordedBy(actor);
        c.setReceiptNumber(newReceiptNumber());
        c = contributions.save(c);

        audit.log(actor, "RECORD_CONTRIBUTION", "Contribution", String.valueOf(c.getId()),
                c.getReceiptNumber() + ": " + c.getAmount() + " RWF from " + member.getFullName());
        notifications.notifyMember(member, "We received your contribution of " + c.getAmount()
                + " RWF dated " + c.getContributionDate() + ". Receipt: " + c.getReceiptNumber() + ".");
        return toResponse(c);
    }

    /** The only way to correct a contribution: it stays on file, marked REVERSED with a reason. */
    @Transactional
    @CacheEvict(cacheNames = "groupSummary", allEntries = true)
    public ContributionDtos.Response reverse(Long id, String reason, String actor) {
        Contribution c = contributions.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contribution " + id + " not found"));
        if (c.getStatus() == ContributionStatus.REVERSED) {
            throw new IllegalStateException("Contribution " + c.getReceiptNumber() + " was already reversed");
        }
        c.setStatus(ContributionStatus.REVERSED);
        c.setReversalReason(reason.trim());
        c.setReversedBy(actor);
        c.setReversedAt(LocalDateTime.now());

        audit.log(actor, "REVERSE_CONTRIBUTION", "Contribution", String.valueOf(id),
                c.getReceiptNumber() + " reversed: " + reason.trim());
        notifications.notifyMember(c.getMember(), "Your contribution " + c.getReceiptNumber()
                + " of " + c.getAmount() + " RWF was reversed. Reason: " + reason.trim());
        return toResponse(c);
    }

    @Transactional(readOnly = true)
    public PageResponse<ContributionDtos.Response> list(Long memberId, Pageable pageable) {
        Page<Contribution> page = memberId == null
                ? contributions.findAll(pageable)
                : contributions.findByMemberId(memberId, pageable);
        return PageResponse.of(page.map(ContributionService::toResponse));
    }

    static String newReceiptNumber() {
        return "RCT-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-"
                + UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
    }

    static ContributionDtos.Response toResponse(Contribution c) {
        Member m = c.getMember();
        return new ContributionDtos.Response(c.getId(), c.getReceiptNumber(), m.getId(), m.getFullName(),
                m.getGroup().getGroupName(), c.getAmount(), c.getContributionDate(), c.getRecordedBy(),
                c.getRecordedAt(), c.getStatus().name(), c.getReversalReason(), c.getReversedBy(), c.getReversedAt());
    }
}