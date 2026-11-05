package pe.edu.upc.center.creditotiendas.audit.domain.model.queries;

import pe.edu.upc.center.creditotiendas.audit.domain.model.valueobjects.OperationActions;

import java.time.Instant;

/**
 * Filterable listing of the operation log for the Admin Sistema console (US-16).
 *
 * <p>Every field is optional; {@code null} means "no filter on this axis". The repository
 * returns results ordered by {@code occurredAt} descending (most recent first).</p>
 *
 * @param userId null → any user
 * @param action null → any action
 * @param entity null → any entity; matched case-insensitively
 * @param from   null → open-ended lower bound
 * @param to     null → open-ended upper bound
 */
public record GetOperationLogsQuery(Long userId, OperationActions action, String entity,
                                    Instant from, Instant to) {
}
