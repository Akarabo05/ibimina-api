package rw.ac.auca.Ibimina_api.security;

import rw.ac.auca.Ibimina_api.entity.AppUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class JwtService {

    private final JwtEncoder encoder;
    private final Duration lifetime;

    public JwtService(JwtEncoder encoder, @Value("${app.jwt.expiration-minutes}") long minutes) {
        this.encoder = encoder;
        this.lifetime = Duration.ofMinutes(minutes);
    }

    public String generate(AppUser user) {
        Instant now = Instant.now();
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer("ibimina-api")
                .subject(user.getUsername())
                .issuedAt(now)
                .expiresAt(now.plus(lifetime))
                .claim("roles", List.of("ROLE_" + user.getRole().name()));
        if (user.getMember() != null) {
            claims.claim("memberId", String.valueOf(user.getMember().getId()));
        }
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims.build())).getTokenValue();
    }

    public long expiresInSeconds() {
        return lifetime.toSeconds();
    }
}