package pe.edu.upc.center.creditotiendas.iam.application.internal.eventhandlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import pe.edu.upc.center.creditotiendas.iam.application.commandservices.RoleCommandService;
import pe.edu.upc.center.creditotiendas.iam.application.commandservices.UserCommandService;
import pe.edu.upc.center.creditotiendas.iam.domain.model.commands.SeedRolesCommand;
import pe.edu.upc.center.creditotiendas.iam.domain.model.commands.SeedSystemAdminCommand;

/**
 * Seeds IAM data at startup: the roles, then the initial system admin.
 *
 * <p>The admin credentials come from {@code iam.seed.system-admin.*} ({@code ADMIN_EMAIL} and
 * {@code ADMIN_PASSWORD}), never from code. If they are not set, seeding is skipped with a
 * warning. Invalid values (e.g. a short password) stop the startup on purpose.</p>
 *
 * <p>Explicit bean name because other contexts will have their own {@code ApplicationReadyEventHandler}.</p>
 */
@Slf4j
@Service("iamApplicationReadyEventHandler")
public class ApplicationReadyEventHandler {

    private final RoleCommandService roleCommandService;
    private final UserCommandService userCommandService;
    private final String systemAdminEmail;
    private final String systemAdminPassword;

    public ApplicationReadyEventHandler(RoleCommandService roleCommandService,
                                        UserCommandService userCommandService,
                                        @Value("${iam.seed.system-admin.email:}") String systemAdminEmail,
                                        @Value("${iam.seed.system-admin.password:}") String systemAdminPassword) {
        this.roleCommandService = roleCommandService;
        this.userCommandService = userCommandService;
        this.systemAdminEmail = systemAdminEmail;
        this.systemAdminPassword = systemAdminPassword;
    }

    @EventListener
    public void on(ApplicationReadyEvent event) {
        roleCommandService.handle(new SeedRolesCommand());

        if (systemAdminEmail.isBlank() || systemAdminPassword.isBlank()) {
            log.warn("ADMIN_EMAIL / ADMIN_PASSWORD not set: the initial system admin was not seeded");
            return;
        }
        userCommandService.handle(new SeedSystemAdminCommand(
                systemAdminEmail, systemAdminPassword, "System", "Administrator"));
    }
}
