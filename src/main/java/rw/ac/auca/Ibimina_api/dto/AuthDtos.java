package rw.ac.auca.Ibimina_api.dto;

import rw.ac.auca.Ibimina_api.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
    private AuthDtos() {}

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}

    public record TokenResponse(String token, String tokenType, String username, String role,
                                Long memberId, long expiresInSeconds) {}

    public record CreateUserRequest(
            @NotBlank @Size(min = 3, max = 40) String username,
            @NotBlank @Size(min = 8, max = 72, message = "Password must be at least 8 characters") String password,
            @Email String email,
            @NotNull Role role,
            Long memberId) {}

    public record UserResponse(Long id, String username, String email, String role, Long memberId, boolean enabled) {}
}