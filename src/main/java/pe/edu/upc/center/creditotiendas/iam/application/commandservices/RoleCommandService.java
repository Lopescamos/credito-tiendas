package pe.edu.upc.center.creditotiendas.iam.application.commandservices;

import pe.edu.upc.center.creditotiendas.iam.domain.model.commands.SeedRolesCommand;

public interface RoleCommandService {
    void handle(SeedRolesCommand command);
}
