package pe.edu.upc.center.creditotiendas.storemanagement.application.internal.outboundservices.acl;

import org.springframework.stereotype.Service;
import pe.edu.upc.center.creditotiendas.iam.interfaces.acl.IamContextFacade;

import java.util.Optional;

/**
 * Store Management's view of IAM: resolves who the authenticated user is and which stores they manage.
 */
@Service
public class ExternalIamService {

    private final IamContextFacade iamContextFacade;

    public ExternalIamService(IamContextFacade iamContextFacade) {
        this.iamContextFacade = iamContextFacade;
    }

    public Optional<Long> fetchUserIdByEmail(String email) {
        var userId = iamContextFacade.fetchUserIdByEmail(email);
        return userId == 0L ? Optional.empty() : Optional.of(userId);
    }

    public boolean isUserAssignedToStore(Long userId, Long storeId) {
        return iamContextFacade.isUserAssignedToStore(userId, storeId);
    }
}
