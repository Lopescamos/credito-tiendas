package pe.edu.upc.center.creditotiendas.iam.application.internal.outboundservices.tokens;

import java.util.Optional;

/**
 * Issues signed, expiring access tokens for authenticated users and reads them back.
 * Implementations decide the token format and signing algorithm; callers only rely on
 * {@link #extractSubject} returning empty for any token that must not be trusted.
 */
public interface TokenService {

    /**
     * @param subject the user email, used to load the user on each request
     * @param userId  added as a claim for the frontend
     * @param role    added as a claim for the frontend; authorization always uses the role in the database
     * @return a signed token
     */
    String generateToken(String subject, Long userId, String role);

    /**
     * @return the subject when the token is well formed, correctly signed, issued by this
     *         application and not expired; empty otherwise
     */
    Optional<String> extractSubject(String token);
}
