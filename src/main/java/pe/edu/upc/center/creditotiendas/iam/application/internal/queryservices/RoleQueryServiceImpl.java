package pe.edu.upc.center.creditotiendas.iam.application.internal.queryservices;

import org.springframework.stereotype.Service;
import pe.edu.upc.center.creditotiendas.iam.application.queryservices.RoleQueryService;
import pe.edu.upc.center.creditotiendas.iam.domain.model.aggregates.Role;
import pe.edu.upc.center.creditotiendas.iam.domain.model.queries.GetAllRolesQuery;
import pe.edu.upc.center.creditotiendas.iam.domain.repositories.RoleRepository;

import java.util.List;

@Service
public class RoleQueryServiceImpl implements RoleQueryService {

    private final RoleRepository roleRepository;

    public RoleQueryServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public List<Role> handle(GetAllRolesQuery query) {
        return roleRepository.findAll();
    }
}
