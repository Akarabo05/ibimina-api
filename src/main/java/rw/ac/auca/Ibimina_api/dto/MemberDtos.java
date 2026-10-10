package rw.ac.auca.Ibimina_api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class MemberDtos {
    private MemberDtos() {}

    public record Request(
            @NotBlank @Size(min = 2, max = 50)
            @Pattern(regexp = "^[\\p{L} '\\-]+$", message = "Full name may only contain letters, spaces, apostrophes and hyphens")
            String fullName,
            @NotBlank @Pattern(regexp = "^07[0-9]{8}$", message = "Phone number must be in the format 07XXXXXXXX")
            String phoneNumber,
            @Email String email,
            @NotNull Long groupId) {}

    public record Response(Long id, String fullName, String phoneNumber, String email,
                           Long groupId, String groupName, boolean active) {}
}