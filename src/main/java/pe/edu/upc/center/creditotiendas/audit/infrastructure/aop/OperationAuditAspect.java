package pe.edu.upc.center.creditotiendas.audit.infrastructure.aop;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import pe.edu.upc.center.creditotiendas.audit.application.commandservices.OperationLogCommandService;
import pe.edu.upc.center.creditotiendas.audit.domain.model.commands.RegisterOperationCommand;
import pe.edu.upc.center.creditotiendas.audit.domain.model.valueobjects.OperationActions;
import pe.edu.upc.center.creditotiendas.iam.application.commandservices.AuthenticatedUser;
import pe.edu.upc.center.creditotiendas.iam.domain.model.commands.SignInCommand;
import pe.edu.upc.center.creditotiendas.iam.interfaces.acl.IamContextFacade;
import pe.edu.upc.center.creditotiendas.shared.application.result.Result;

import java.lang.reflect.RecordComponent;
import java.time.Instant;

/**
 * Writes an entry in {@code operation_logs} after any audited command handler returns (US-16).
 *
 * <p>Two pointcuts:
 * <ul>
 *   <li>{@link #handleCommand} — every {@code handle(XxxCommand)} in a {@code *CommandServiceImpl}
 *       of any bounded context. The command's class name drives the action (verb prefix) and the
 *       entity (middle), and the result is only logged when it is a {@link Result.Success}.</li>
 *   <li>{@link #handleSignIn} — the specific overload that receives a {@link SignInCommand},
 *       logged as {@code SIGN_IN} on success and {@code SIGN_IN_FAILED} on failure, because
 *       failed login attempts are part of what US-16 must record.</li>
 * </ul>
 *
 * <p>Seed commands and the audit service itself are excluded to avoid noise and infinite loops.
 * Writes go through {@link OperationLogCommandService}, which runs in a {@code REQUIRES_NEW}
 * transaction and swallows persistence errors — a failed audit never breaks the business command
 * that just succeeded.</p>
 */
@Slf4j
@Aspect
@Component
public class OperationAuditAspect {

    private final OperationLogCommandService operationLogCommandService;
    private final IamContextFacade iamContextFacade;

    public OperationAuditAspect(OperationLogCommandService operationLogCommandService,
                                IamContextFacade iamContextFacade) {
        this.operationLogCommandService = operationLogCommandService;
        this.iamContextFacade = iamContextFacade;
    }

    /**
     * Any {@code handle(..)} method on a class whose name ends with {@code CommandServiceImpl},
     * anywhere in the project. {@link #handleSignIn} covers login separately and the audit
     * service itself is excluded to break the recursion.
     */
    @Pointcut("execution(* pe.edu.upc.center.creditotiendas..application.internal.commandservices.*CommandServiceImpl.handle(..))"
            + " && !within(pe.edu.upc.center.creditotiendas.audit..*)")
    public void auditedCommandHandler() {
    }

    @AfterReturning(pointcut = "auditedCommandHandler()", returning = "returned")
    public void handleCommand(Object[] args, Object returned) {
        if (args == null || args.length == 0 || args[0] == null) return;
        var command = args[0];
        if (command instanceof SignInCommand) return;                          // handled by handleSignIn
        var action = actionFromCommand(command);
        if (action == null) return;                                            // seed/simulate/etc. are not audited

        // If the handler returned a Result, only log successes; void or domain returns are logged as-is.
        if (returned instanceof Result<?, ?> result && !result.isSuccess()) return;

        try {
            operationLogCommandService.handle(new RegisterOperationCommand(
                    currentUserId(), action, entityFromCommand(command), entityIdFromCommand(command),
                    command.getClass().getSimpleName(), currentIpAddress(), Instant.now()));
        } catch (Exception e) {
            log.warn("Audit aspect failed for command {}: {}", command.getClass().getSimpleName(), e.getMessage());
        }
    }

    @AfterReturning(
            pointcut = "execution(* pe.edu.upc.center.creditotiendas.iam..UserCommandServiceImpl.handle(..)) "
                    + "&& args(command)",
            returning = "returned")
    public void handleSignIn(SignInCommand command, Object returned) {
        boolean success = returned instanceof Result<?, ?> result && result.isSuccess();
        var action = success ? OperationActions.SIGN_IN : OperationActions.SIGN_IN_FAILED;
        Long userId = success && returned instanceof Result.Success<?, ?> ok && ok.value() instanceof AuthenticatedUser au
                ? au.user().getId() : null;
        try {
            operationLogCommandService.handle(new RegisterOperationCommand(
                    userId, action, "User", null,
                    "SignInCommand(%s)".formatted(command.email()),
                    currentIpAddress(), Instant.now()));
        } catch (Exception e) {
            log.warn("Audit aspect failed for SignInCommand: {}", e.getMessage());
        }
    }

    /**
     * Picks an {@link OperationActions} from the command's class-name prefix. Returns {@code null}
     * for commands that are not business-visible writes ({@code SeedXxxCommand},
     * {@code SimulateCreditSaleCommand}, {@code MarkOverdueInstallmentsCommand}, etc.), so the
     * log stays focused on what US-16 cares about: CRUD and login done by a real user.
     */
    static OperationActions actionFromCommand(Object command) {
        var name = command.getClass().getSimpleName();
        if (name.startsWith("Create")) return OperationActions.CREATE;
        if (name.startsWith("Update") && name.endsWith("StatusCommand")) return OperationActions.PATCH;
        if (name.startsWith("Update")) return OperationActions.UPDATE;
        if (name.startsWith("Delete")) return OperationActions.DELETE;
        if (name.startsWith("Register")) return OperationActions.REGISTER;
        if (name.startsWith("Approve")) return OperationActions.APPROVE;
        if (name.startsWith("Reject")) return OperationActions.REJECT;
        if (name.startsWith("Assign")) return OperationActions.ASSIGN;
        if (name.startsWith("Unassign")) return OperationActions.UNASSIGN;
        if (name.startsWith("Upload")) return OperationActions.UPLOAD;
        return null;
    }

    /**
     * Strips the verb prefix and {@code Command} suffix to leave the aggregate name:
     * {@code CreateProductCommand → Product}, {@code ApproveCreditPurchaseRequestCommand →
     * CreditPurchaseRequest}. The result matches the aggregate root name, which is enough for the
     * Admin Sistema console to filter by.
     */
    static String entityFromCommand(Object command) {
        var name = command.getClass().getSimpleName();
        for (var prefix : new String[]{"Create", "UpdateStatus", "Update", "Delete", "Register", "Approve", "Reject",
                "Assign", "Unassign", "Upload"}) {
            if (name.startsWith(prefix)) {
                name = name.substring(prefix.length());
                break;
            }
        }
        if (name.endsWith("StatusCommand")) name = name.substring(0, name.length() - "StatusCommand".length());
        else if (name.endsWith("Command")) name = name.substring(0, name.length() - "Command".length());
        return name.isBlank() ? "Unknown" : name;
    }

    /**
     * First record component ending in {@code Id} in the command (record), used when the operation
     * updates an existing aggregate. Returns {@code null} for create commands, which don't know the
     * new id yet.
     */
    static Long entityIdFromCommand(Object command) {
        if (!command.getClass().isRecord()) return null;
        try {
            for (RecordComponent rc : command.getClass().getRecordComponents()) {
                if (rc.getName().endsWith("Id") && Long.class.equals(rc.getType())) {
                    var value = rc.getAccessor().invoke(command);
                    if (value != null) return (Long) value;
                }
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return null;
    }

    private Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) return null;
        var email = authentication.getName();
        if (email == null || email.isBlank() || "anonymousUser".equals(email)) return null;
        var id = iamContextFacade.fetchUserIdByEmail(email);
        return id == 0L ? null : id;
    }

    private String currentIpAddress() {
        var attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servlet) {
            HttpServletRequest request = servlet.getRequest();
            var forwarded = request.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) return forwarded.split(",")[0].trim();
            return request.getRemoteAddr();
        }
        return null;
    }
}
