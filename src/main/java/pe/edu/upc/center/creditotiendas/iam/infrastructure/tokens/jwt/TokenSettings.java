package pe.edu.upc.center.creditotiendas.iam.infrastructure.tokens.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT settings bound from {@code jwt.*} ({@code JWT_SECRET} and {@code JWT_EXPIRATION}
 * environment variables). Validated by {@link TokenServiceImpl} at startup.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")
public class TokenSettings {

    /** HMAC signing secret. At least 32 bytes (256 bits) for HS256. */
    private String secret;

    /** Token lifetime in milliseconds. */
    private long expiration;

    /** Value of the {@code iss} claim; tokens from another issuer are rejected. */
    private String issuer = "creditotiendas-backend";
}
