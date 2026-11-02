package pe.edu.upc.center.creditotiendas.iam.infrastructure.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import pe.edu.upc.center.creditotiendas.iam.domain.model.valueobjects.Roles;

/**
 * Authorization rule for user creation (US-14), used from {@code UsersController} as
 * {@code @PreAuthorize("@iamAuthorization.canCreateUserWithRole(authentication, #resource.role)")}.
 *
 * <p>The system admin creates users with any role assignable through user management; a store
 * admin registers their own customers in person (US-04) and may only create {@code CLIENT}
 * users, never another store admin.</p>
 */
@Component("iamAuthorization")
public class IamAuthorization {

    private static final String ROLE_SYSTEM_ADMIN = "ROLE_SYSTEM_ADMIN";

    public boolean canCreateUserWithRole(Authentication authentication, Roles role) {
        return hasRole(authentication, ROLE_SYSTEM_ADMIN) || role == Roles.CLIENT;
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> role.equals(authority.getAuthority()));
    }
}
