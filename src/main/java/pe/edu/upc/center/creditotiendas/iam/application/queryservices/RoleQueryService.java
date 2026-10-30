package pe.edu.upc.center.creditotiendas.iam.application.queryservices;

import pe.edu.upc.center.creditotiendas.iam.domain.model.aggregates.Role;
import pe.edu.upc.center.creditotiendas.iam.domain.model.queries.GetAllRolesQuery;

import java.util.List;

public interface RoleQueryService {
    List<Role> handle(GetAllRolesQuery query);
}
