package rw.ac.auca.Ibimina_api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public final class GroupDtos {
    private GroupDtos() {}

    public record Request(
            @NotBlank @Size(min = 2, max = 60) String groupName,
            @NotBlank @Pattern(regexp = "WEEKLY|BIWEEKLY|MONTHLY", message = "Meeting frequency must be WEEKLY, BIWEEKLY or MONTHLY")
            String meetingFrequency,
            @NotNull @DecimalMin("100.00") BigDecimal contributionAmount) {}

    public record Response(Long id, String groupName, String meetingFrequency, BigDecimal contributionAmount) {}
}