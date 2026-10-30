package pe.edu.upc.center.creditotiendas.iam.application.commandservices;

import pe.edu.upc.center.creditotiendas.iam.domain.model.aggregates.User;
import pe.edu.upc.center.creditotiendas.iam.domain.model.commands.*;
import pe.edu.upc.center.creditotiendas.shared.application.result.ApplicationError;
import pe.edu.upc.center.creditotiendas.shared.application.result.Result;

public interface UserCommandService {
    Result<AuthenticatedUser, ApplicationError> handle(SignInCommand command);
    Result<Long, ApplicationError> handle(CreateUserCommand command);
    Result<User, ApplicationError> handle(UpdateUserCommand command);
    Result<User, ApplicationError> handle(UpdateUserStatusCommand command);
    Result<User, ApplicationError> handle(AssignUserToStoreCommand command);
    Result<User, ApplicationError> handle(UnassignUserFromStoreCommand command);
    void handle(SeedSystemAdminCommand command);
}
