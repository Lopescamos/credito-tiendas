package pe.edu.upc.center.creditotiendas.audit.domain.model.aggregates;

import org.junit.jupiter.api.Test;
import pe.edu.upc.center.creditotiendas.audit.domain.model.commands.RegisterOperationCommand;
import pe.edu.upc.center.creditotiendas.audit.domain.model.valueobjects.OperationActions;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OperationLogTest {

    @Test
    void buildsFromCommandAndKeepsAllFields() {
        var now = Instant.parse("2026-01-02T10:00:00Z");
        var command = new RegisterOperationCommand(7L, OperationActions.CREATE, "Product", 42L,
                "CreateProductCommand", "10.0.0.1", now);

        var log = new OperationLog(command);

        assertThat(log.getUserId()).isEqualTo(7L);
        assertThat(log.getAction()).isEqualTo(OperationActions.CREATE);
        assertThat(log.getEntity()).isEqualTo("Product");
        assertThat(log.getEntityId()).isEqualTo(42L);
        assertThat(log.getDetail()).isEqualTo("CreateProductCommand");
        assertThat(log.getIpAddress()).isEqualTo("10.0.0.1");
        assertThat(log.getOccurredAt()).isEqualTo(now);
    }

    @Test
    void defaultsOccurredAtToNowWhenCommandHasNoTimestamp() {
        var before = Instant.now();
        var command = new RegisterOperationCommand(null, OperationActions.SIGN_IN_FAILED, "User", null,
                "SignInCommand(ghost@example.com)", "127.0.0.1", null);

        var log = new OperationLog(command);

        assertThat(log.getUserId()).isNull();
        assertThat(log.getOccurredAt()).isBetween(before, Instant.now());
    }

    @Test
    void commandRejectsNullActionOrBlankEntity() {
        assertThatThrownBy(() -> new RegisterOperationCommand(1L, null, "Product", null, "d", null, null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new RegisterOperationCommand(1L, OperationActions.CREATE, " ", null, "d", null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
