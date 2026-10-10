package rw.ac.auca.Ibimina_api.service;

import rw.ac.auca.Ibimina_api.audit.AuditService;
import rw.ac.auca.Ibimina_api.dto.AuthDtos;
import rw.ac.auca.Ibimina_api.entity.AppUser;
import rw.ac.auca.Ibimina_api.entity.Member;
import rw.ac.auca.Ibimina_api.entity.Role;
import rw.ac.auca.Ibimina_api.exception.ResourceNotFoundException;
import rw.ac.auca.Ibimina_api.repository.AppUserRepository;
import rw.ac.auca.Ibimina_api.repository.MemberRepository;
import rw.ac.auca.Ibimina_api.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthService {

    private final AppUserRepository users;
    private final MemberRepository members;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final AuditService audit;

    public AuthService(AppUserRepository users, MemberRepository members, PasswordEncoder encoder,
                       JwtService jwt, AuditService audit) {
        this.users = users;
        this.members = members;
        this.encoder = encoder;
        this.jwt = jwt;
        this.audit = audit;
    }

    @Transactional(readOnly = true)
    public AuthDtos.TokenResponse login(AuthDtos.LoginRequest r) {
        AppUser user = users.findByUsername(r.username())
                .filter(AppUser::isEnabled)
                .filter(u -> encoder.matches(r.password(), u.getPasswordHash()))
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));
        audit.log(user.getUsername(), "LOGIN", "AppUser", String.valueOf(user.getId()), "Signed in with password");
        Long memberId = user.getMember() == null ? null : user.getMember().getId();
        return new AuthDtos.TokenResponse(jwt.generate(user), "Bearer", user.getUsername(),
                user.getRole().name(), memberId, jwt.expiresInSeconds());
    }

    @Transactional
    public AuthDtos.UserResponse createUser(AuthDtos.CreateUserRequest r, String actor) {
        if (users.existsByUsername(r.username())) {
            throw new IllegalArgumentException("Username '" + r.username() + "' is already taken");
        }
        String email = r.email() == null || r.email().isBlank() ? null : r.email().trim();
        if (email != null && users.existsByEmail(email)) {
            throw new IllegalArgumentException("E-mail '" + email + "' is already used by another account");
        }
        AppUser u = new AppUser();
        u.setUsername(r.username().trim());
        u.setPasswordHash(encoder.encode(r.password()));
        u.setEmail(email);
        u.setRole(r.role());
        if (r.role() == Role.MEMBER) {
            if (r.memberId() == null) throw new IllegalArgumentException("MEMBER accounts must be linked to a member");
            Member m = members.findById(r.memberId())
                    .orElseThrow(() -> new ResourceNotFoundException("Member " + r.memberId() + " not found"));
            u.setMember(m);
        }
        u = users.save(u);
        audit.log(actor, "CREATE_USER", "AppUser", String.valueOf(u.getId()), "Created " + u.getRole() + " account " + u.getUsername());
        return toResponse(u);
    }

    @Transactional(readOnly = true)
    public List<AuthDtos.UserResponse> listUsers() {
        return users.findAll().stream().map(AuthService::toResponse).toList();
    }

    @Transactional
    public AuthDtos.UserResponse setEnabled(Long id, boolean enabled, String actor) {
        AppUser u = users.findById(id).orElseThrow(() -> new ResourceNotFoundException("User " + id + " not found"));
        if (u.getUsername().equals(actor) && !enabled) {
            throw new IllegalArgumentException("You cannot disable your own account");
        }
        u.setEnabled(enabled);
        audit.log(actor, enabled ? "ENABLE_USER" : "DISABLE_USER", "AppUser", String.valueOf(id), u.getUsername());
        return toResponse(u);
    }

    private static AuthDtos.UserResponse toResponse(AppUser u) {
        return new AuthDtos.UserResponse(u.getId(), u.getUsername(), u.getEmail(), u.getRole().name(),
                u.getMember() == null ? null : u.getMember().getId(), u.isEnabled());
    }
}