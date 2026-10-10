package rw.ac.auca.Ibimina_api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class ContributionDtos {
    private ContributionDtos() {}

    public record Request(
            @NotNull Long memberId,
            @NotNull @DecimalMin(value = "100.00", message = "Amount must be at least 100") BigDecimal amount,
            @NotNull LocalDate contributionDate) {}

    public record ReverseRequest(@NotBlank @Size(min = 5, max = 255, message = "Give a reason of at least 5 characters") String reason) {}

    public record Response(Long id, String receiptNumber, Long memberId, String memberName, String groupName,
                           BigDecimal amount, LocalDate contributionDate, String recordedBy, LocalDateTime recordedAt,
                           String status, String reversalReason, String reversedBy, LocalDateTime reversedAt) {}
}