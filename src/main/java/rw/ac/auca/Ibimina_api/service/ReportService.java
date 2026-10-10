package rw.ac.auca.Ibimina_api.service;

import rw.ac.auca.Ibimina_api.dto.MemberDtos;
import rw.ac.auca.Ibimina_api.dto.ReportDtos;
import rw.ac.auca.Ibimina_api.entity.ContributionStatus;
import rw.ac.auca.Ibimina_api.entity.Group;
import rw.ac.auca.Ibimina_api.repository.ContributionRepository;
import rw.ac.auca.Ibimina_api.repository.MemberRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class ReportService {

    private final GroupService groupService;
    private final MemberRepository members;
    private final ContributionRepository contributions;

    public ReportService(GroupService groupService, MemberRepository members, ContributionRepository contributions) {
        this.groupService = groupService;
        this.members = members;
        this.contributions = contributions;
    }

    /** Cached; every recorded or reversed contribution evicts the cache. */
    @Cacheable(cacheNames = "groupSummary", key = "#groupId + '-' + #month")
    @Transactional(readOnly = true)
    public ReportDtos.GroupSummary groupSummary(Long groupId, YearMonth month) {
        Group group = groupService.find(groupId);
        LocalDate from = month.atDay(1);
        LocalDate to = month.atEndOfMonth();

        long activeMembers = members.countByGroupIdAndActiveTrue(groupId);
        BigDecimal expected = group.getContributionAmount().multiply(BigDecimal.valueOf(activeMembers));
        BigDecimal collected = orZero(contributions.sumByGroupBetween(groupId, ContributionStatus.ACTIVE, from, to));
        BigDecimal total = orZero(contributions.sumByGroup(groupId, ContributionStatus.ACTIVE));
        double rate = expected.signum() == 0 ? 0.0
                : collected.multiply(BigDecimal.valueOf(100)).divide(expected, 1, RoundingMode.HALF_UP).doubleValue();

        List<MemberDtos.Response> unpaid = members.findUnpaid(groupId, ContributionStatus.ACTIVE, from, to).stream()
                .map(MemberService::toResponse).toList();

        return new ReportDtos.GroupSummary(groupId, group.getGroupName(), month.toString(), activeMembers,
                expected, collected, rate, total, unpaid);
    }

    private static BigDecimal orZero(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}