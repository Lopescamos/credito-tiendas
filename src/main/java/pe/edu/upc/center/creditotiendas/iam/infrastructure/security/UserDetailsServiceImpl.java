package pe.edu.upc.center.creditotiendas.iam.infrastructure.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pe.edu.upc.center.creditotiendas.iam.application.queryservices.UserQueryService;
import pe.edu.upc.center.creditotiendas.iam.domain.model.queries.GetUserByEmailQuery;
import pe.edu.upc.center.creditotiendas.iam.domain.model.valueobjects.EmailAddress;

/**
 * Loads users by email (the JWT subject). Declaring this bean also disables Spring Boot's
 * in-memory default user with its generated password.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserQueryService userQueryService;

    public UserDetailsServiceImpl(UserQueryService userQueryService) {
        this.userQueryService = userQueryService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        EmailAddress email;
        try {
            email = new EmailAddress(username);
        } catch (IllegalArgumentException e) {
            throw new UsernameNotFoundException("User not found");
        }
        return userQueryService.handle(new GetUserByEmailQuery(email))
                .map(UserDetailsImpl::build)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
