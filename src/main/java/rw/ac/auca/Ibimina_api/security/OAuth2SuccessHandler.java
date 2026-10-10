package rw.ac.auca.Ibimina_api.security;

import rw.ac.auca.Ibimina_api.audit.AuditService;
import rw.ac.auca.Ibimina_api.entity.AppUser;
import rw.ac.auca.Ibimina_api.repository.AppUserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/** After Google sign-in: only e-mails already registered by an admin may enter. Issues our own JWT. */
@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final AppUserRepository users;
    private final JwtService jwtService;
    private final AuditService audit;
    private final String frontendUrl;

    public OAuth2SuccessHandler(AppUserRepository users, JwtService jwtService, AuditService audit,
                                @Value("${app.frontend-url}") String frontendUrl) {
        this.users = users;
        this.jwtService = jwtService;
        this.audit = audit;
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User principal = (OAuth2User) authentication.getPrincipal();
        String email = principal.getAttribute("email");
        Optional<AppUser> user = email == null ? Optional.empty() : users.findByEmail(email);

        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        SecurityContextHolder.clearContext();

        if (user.isEmpty() || !user.get().isEnabled()) {
            String msg = URLEncoder.encode("This Google account is not registered in Ibimina", StandardCharsets.UTF_8);
            response.sendRedirect(frontendUrl + "/login?error=" + msg);
            return;
        }
        audit.log(user.get().getUsername(), "LOGIN_GOOGLE", "AppUser", String.valueOf(user.get().getId()), "Signed in with Google");
        response.sendRedirect(frontendUrl + "/auth/callback#token=" + jwtService.generate(user.get()));
    }
}