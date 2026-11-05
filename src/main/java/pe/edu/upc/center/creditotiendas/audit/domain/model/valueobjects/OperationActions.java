package pe.edu.upc.center.creditotiendas.audit.domain.model.valueobjects;

/**
 * Action recorded in an {@code OperationLog}. Login (success and failure) and the write verbs that
 * a command service exposes, derived from the command's class name by the audit aspect.
 */
public enum OperationActions {
    SIGN_IN,
    SIGN_IN_FAILED,
    CREATE,
    UPDATE,
    DELETE,
    PATCH,
    APPROVE,
    REJECT,
    REGISTER,
    ASSIGN,
    UNASSIGN,
    UPLOAD
}
