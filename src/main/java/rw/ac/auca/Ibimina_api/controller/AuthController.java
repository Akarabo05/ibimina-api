package rw.ac.auca.Ibimina_api.controller;

import rw.ac.auca.Ibimina_api.dto.AuthDtos;
import rw.ac.auca.Ibimina_api.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/auth/login")
    public AuthDtos.TokenResponse login(@Valid @RequestBody AuthDtos.LoginRequest request) {
        return auth.login(request);
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public AuthDtos.UserResponse createUser(@Valid @RequestBody AuthDtos.CreateUserRequest request,
                                            @AuthenticationPrincipal Jwt jwt) {
        return auth.createUser(request, jwt.getSubject());
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AuthDtos.UserResponse> listUsers() {
        return auth.listUsers();
    }

    @PatchMapping("/users/{id}/enabled")
    @PreAuthorize("hasRole('ADMIN')")
    public AuthDtos.UserResponse setEnabled(@PathVariable Long id, @RequestParam boolean value,
                                            @AuthenticationPrincipal Jwt jwt) {
        return auth.setEnabled(id, value, jwt.getSubject());
    }
}