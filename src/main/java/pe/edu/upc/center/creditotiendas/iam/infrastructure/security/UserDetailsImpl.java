package pe.edu.upc.center.creditotiendas.iam.infrastructure.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pe.edu.upc.center.creditotiendas.iam.domain.model.aggregates.User;

import java.util.Collection;
import java.util.List;

/**
 * Spring Security view of a {@link User}. The role becomes the authority {@code ROLE_<role>},
 * so controllers can use {@code @PreAuthorize("hasRole('SYSTEM_ADMIN')")}.
 */
@Getter
public class UserDetailsImpl implements UserDetails {

    private final Long id;
    private final String username;
    private final String password;
    private final boolean enabled;
    private final Collection<? extends GrantedAuthority> authorities;

    private UserDetailsImpl(Long id, String username, String password, boolean enabled,
                            Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.enabled = enabled;
        this.authorities = authorities;
    }

    public static UserDetailsImpl build(User user) {
        return new UserDetailsImpl(
                user.getId(),
                user.getEmail().value(),
                user.getPasswordHash(),
                user.isActive(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
    }
}
