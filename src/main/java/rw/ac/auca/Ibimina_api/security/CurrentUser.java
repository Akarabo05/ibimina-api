package rw.ac.auca.Ibimina_api.security;

import org.springframework.security.oauth2.jwt.Jwt;

public final class CurrentUser {
    private CurrentUser() {}

    public static String username(Jwt jwt) {
        return jwt.getSubject();
    }

    public static Long requireMemberId(Jwt jwt) {
        String id = jwt.getClaimAsString("memberId");
        if (id == null) {
            throw new IllegalArgumentException("This account is not linked to a member");
        }
        return Long.valueOf(id);
    }
}