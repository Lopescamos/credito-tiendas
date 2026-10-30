package pe.edu.upc.center.creditotiendas.iam.infrastructure.tokens.jwt;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.upc.center.creditotiendas.iam.application.internal.outboundservices.tokens.TokenService;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

/**
 * JWT implementation of {@link TokenService}: HMAC-signed tokens with subject, issuer,
 * issued-at and expiration.
 *
 * <p>Fails at startup when the secret is missing or shorter than 32 bytes, instead of
 * running with a weak or empty key.</p>
 */
@Slf4j
@Service
public class TokenServiceImpl implements TokenService {

    private static final int MIN_SECRET_BYTES = 32;

    private final SecretKey signingKey;
    private final JwtParser parser;
    private final long expirationMillis;
    private final String issuer;

    public TokenServiceImpl(TokenSettings settings) {
        var secret = settings.getSecret();
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES)
            throw new IllegalStateException(
                    "jwt.secret (JWT_SECRET) must have at least %d bytes".formatted(MIN_SECRET_BYTES));
        if (settings.getExpiration() <= 0)
            throw new IllegalStateException("jwt.expiration (JWT_EXPIRATION) must be a positive number of milliseconds");

        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMillis = settings.getExpiration();
        this.issuer = settings.getIssuer();
        this.parser = Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(issuer)
                .build();
    }

    @Override
    public String generateToken(String subject, Long userId, String role) {
        var now = new Date();
        return Jwts.builder()
                .issuer(issuer)
                .subject(subject)
                .claim("userId", userId)
                .claim("role", role)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMillis))
                .signWith(signingKey)
                .compact();
    }

    @Override
    public Optional<String> extractSubject(String token) {
        try {
            return Optional.ofNullable(parser.parseSignedClaims(token).getPayload().getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Rejected JWT: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
