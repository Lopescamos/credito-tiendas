package pe.edu.upc.center.creditotiendas.iam.application.queryservices;

import pe.edu.upc.center.creditotiendas.iam.domain.model.aggregates.User;
import pe.edu.upc.center.creditotiendas.iam.domain.model.queries.GetAllUsersQuery;
import pe.edu.upc.center.creditotiendas.iam.domain.model.queries.GetUserByEmailQuery;
import pe.edu.upc.center.creditotiendas.iam.domain.model.queries.GetUserByIdQuery;

import java.util.List;
import java.util.Optional;

public interface UserQueryService {
    Optional<User> handle(GetUserByIdQuery query);
    Optional<User> handle(GetUserByEmailQuery query);
    List<User> handle(GetAllUsersQuery query);
}
