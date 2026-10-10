package rw.ac.auca.Ibimina_api.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class LoanDtos {
    private LoanDtos() {}

    public record Request(
            @NotNull Long memberId,
            @NotNull @DecimalMin("1000.00") BigDecimal amount,
            @NotNull @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal interestRate,
            @NotNull LocalDate dueDate) {}

    public record RepaymentRequest(@NotNull @DecimalMin("1.00") BigDecimal amount) {}

    public record Response(Long id, Long memberId, String memberName, String groupName, BigDecimal principal,
                           BigDecimal interestRate, BigDecimal totalDue, BigDecimal amountPaid, BigDecimal outstanding,
                           LocalDate dueDate, String status, String requestedBy, LocalDateTime requestedAt,
                           String decidedBy, LocalDateTime decidedAt) {}

    public record RepaymentResponse(Long id, BigDecimal amount, LocalDateTime paidAt, String recordedBy) {}
}