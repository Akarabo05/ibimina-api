package rw.ac.auca.Ibimina_api.dto;

import java.math.BigDecimal;
import java.util.List;

public final class ReportDtos {
    private ReportDtos() {}

    public record GroupSummary(Long groupId, String groupName, String month, long activeMembers,
                               BigDecimal expectedInMonth, BigDecimal collectedInMonth,
                               double collectionRatePercent, BigDecimal totalCollected,
                               List<MemberDtos.Response> unpaidMembers) {}
}