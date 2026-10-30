package pe.edu.upc.center.creditotiendas.iam.infrastructure.hashing.bcrypt;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import pe.edu.upc.center.creditotiendas.iam.application.internal.outboundservices.hashing.HashingService;

/**
 * BCrypt implementation of {@link HashingService} (US-14: passwords are stored only as hash).
 */
@Service
public class HashingServiceImpl implements HashingService {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public String encode(CharSequence rawPassword) {
        return encoder.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }
}
