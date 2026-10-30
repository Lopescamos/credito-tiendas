package pe.edu.upc.center.creditotiendas.iam.application.internal.outboundservices.hashing;

/**
 * Hashes and verifies user passwords with a one-way algorithm.
 * Implementations decide the algorithm; callers must never depend on a specific one
 * and must only store the value returned by {@link #encode}.
 */
public interface HashingService {
    String encode(CharSequence rawPassword);
    boolean matches(CharSequence rawPassword, String encodedPassword);
}
