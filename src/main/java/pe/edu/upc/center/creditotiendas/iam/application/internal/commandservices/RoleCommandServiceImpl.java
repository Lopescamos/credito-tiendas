package pe.edu.upc.center.creditotiendas.iam.application.internal.commandservices;

import org.springframework.stereotype.Service;
import pe.edu.upc.center.creditotiendas.iam.application.commandservices.RoleCommandService;
import pe.edu.upc.center.creditotiendas.iam.domain.model.aggregates.Role;
import pe.edu.upc.center.creditotiendas.iam.domain.model.commands.SeedRolesCommand;
import pe.edu.upc.center.creditotiendas.iam.domain.model.valueobjects.Roles;
import pe.edu.upc.center.creditotiendas.iam.domain.repositories.RoleRepository;

import java.util.Arrays;

@Service
public class RoleCommandServiceImpl implements RoleCommandService {

    private final RoleRepository roleRepository;

    public RoleCommandServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    /**
     * Inserts only the missing roles, so a role added to the enum in a later release is created
     * without touching the existing ones.
     */
    @Override
    public void handle(SeedRolesCommand command) {
        Arrays.stream(Roles.values())
                .filter(role -> !roleRepository.existsByName(role))
                .forEach(role -> roleRepository.save(new Role(role)));
    }
}
