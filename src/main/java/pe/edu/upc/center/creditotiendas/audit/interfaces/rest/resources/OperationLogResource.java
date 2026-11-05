package pe.edu.upc.center.creditotiendas.audit.interfaces.rest.resources;

import java.time.Instant;

public record OperationLogResource(
        Long id,
        Long userId,
        String action,
        String entity,
        Long entityId,
        String detail,
        String ipAddress,
        Instant occurredAt) {
}
