package pe.edu.upc.center.creditotiendas.iam.application.acl;

import org.springframework.stereotype.Service;
import pe.edu.upc.center.creditotiendas.iam.application.queryservices.UserQueryService;
import pe.edu.upc.center.creditotiendas.iam.domain.model.aggregates.User;
import pe.edu.upc.center.creditotiendas.iam.domain.model.queries.GetUserByEmailQuery;
import pe.edu.upc.center.creditotiendas.iam.domain.model.queries.GetUserByIdQuery;
import pe.edu.upc.center.creditotiendas.iam.domain.model.valueobjects.EmailAddress;
import pe.edu.upc.center.creditotiendas.iam.interfaces.acl.IamContextFacade;

@Service
public class IamContextFacadeImpl implements IamContextFacade {

    private final UserQueryService userQueryService;

    public IamContextFacadeImpl(UserQueryService userQueryService) {
        this.userQueryService = userQueryService;
    }

    @Override
    public Long fetchUserIdByEmail(String email) {
        try {
            return userQueryService.handle(new GetUserByEmailQuery(new EmailAddress(email)))
                    .map(User::getId)
                    .orElse(0L);
        } catch (IllegalArgumentException e) {
            return 0L;
        }
    }

    @Override
    public String fetchUserRoleByUserId(Long userId) {
        return userQueryService.handle(new GetUserByIdQuery(userId))
                .map(user -> user.getRole().name())
                .orElse("");
    }

    @Override
    public boolean isUserAssignedToStore(Long userId, Long storeId) {
        return userQueryService.handle(new GetUserByIdQuery(userId))
                .map(user -> user.isActive() && user.isAssignedToStore(storeId))
                .orElse(false);
    }
}
