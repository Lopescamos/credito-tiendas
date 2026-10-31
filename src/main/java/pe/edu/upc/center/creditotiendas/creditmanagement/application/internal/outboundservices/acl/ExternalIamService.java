package pe.edu.upc.center.creditotiendas.creditmanagement.application.internal.outboundservices.acl;

import org.springframework.stereotype.Service;
import pe.edu.upc.center.creditotiendas.iam.interfaces.acl.IamContextFacade;

import java.util.Optional;

/**
 * Credit Management's view of IAM: who a user is, their role and the stores they manage.
 *
 * <p>Explicit bean name: Store Management has its own {@code ExternalIamService}.</p>
 */
@Service("creditManagementExternalIamService")
public class ExternalIamService {

    private static final String CLIENT_ROLE = "CLIENT";

    private final IamContextFacade iamContextFacade;

    public ExternalIamService(IamContextFacade iamContextFacade) {
        this.iamContextFacade = iamContextFacade;
    }

    public Optional<Long> fetchUserIdByEmail(String email) {
        var userId = iamContextFacade.fetchUserIdByEmail(email);
        return userId == 0L ? Optional.empty() : Optional.of(userId);
    }

    public boolean isClient(Long userId) {
        return CLIENT_ROLE.equals(iamContextFacade.fetchUserRoleByUserId(userId));
    }

    public boolean isUserAssignedToStore(Long userId, Long storeId) {
        return iamContextFacade.isUserAssignedToStore(userId, storeId);
    }
}
