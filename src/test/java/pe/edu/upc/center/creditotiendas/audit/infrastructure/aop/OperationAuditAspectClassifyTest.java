package pe.edu.upc.center.creditotiendas.audit.infrastructure.aop;

import org.junit.jupiter.api.Test;
import pe.edu.upc.center.creditotiendas.audit.domain.model.valueobjects.OperationActions;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The aspect classifies commands by class-name prefix. These tests pin that mapping so a renamed
 * command (or a new verb) fails here instead of silently producing a wrong action in the log.
 */
class OperationAuditAspectClassifyTest {

    @Test
    void mapsEachKnownPrefixToItsAction() {
        assertThat(OperationAuditAspect.actionFromCommand(new CreateProductCommand(1L))).isEqualTo(OperationActions.CREATE);
        assertThat(OperationAuditAspect.actionFromCommand(new UpdateProductCommand(1L))).isEqualTo(OperationActions.UPDATE);
        assertThat(OperationAuditAspect.actionFromCommand(new UpdateProductStatusCommand(1L))).isEqualTo(OperationActions.PATCH);
        assertThat(OperationAuditAspect.actionFromCommand(new DeleteProductCommand(1L))).isEqualTo(OperationActions.DELETE);
        assertThat(OperationAuditAspect.actionFromCommand(new RegisterCreditSaleCommand(1L))).isEqualTo(OperationActions.REGISTER);
        assertThat(OperationAuditAspect.actionFromCommand(new ApproveCreditPurchaseRequestCommand(1L))).isEqualTo(OperationActions.APPROVE);
        assertThat(OperationAuditAspect.actionFromCommand(new RejectCreditPurchaseRequestCommand(1L))).isEqualTo(OperationActions.REJECT);
        assertThat(OperationAuditAspect.actionFromCommand(new AssignUserToStoreCommand(1L))).isEqualTo(OperationActions.ASSIGN);
        assertThat(OperationAuditAspect.actionFromCommand(new UnassignUserFromStoreCommand(1L))).isEqualTo(OperationActions.UNASSIGN);
        assertThat(OperationAuditAspect.actionFromCommand(new UploadProductImageCommand(1L))).isEqualTo(OperationActions.UPLOAD);
    }

    @Test
    void returnsNullForNonAuditedCommands() {
        assertThat(OperationAuditAspect.actionFromCommand(new SeedRolesCommand())).isNull();
        assertThat(OperationAuditAspect.actionFromCommand(new SimulateCreditSaleCommand(1L))).isNull();
        assertThat(OperationAuditAspect.actionFromCommand(new MarkOverdueInstallmentsCommand())).isNull();
        assertThat(OperationAuditAspect.actionFromCommand(new GenerateAccountStatementCommand())).isNull();
    }

    @Test
    void strippsPrefixAndSuffixToYieldTheAggregateName() {
        assertThat(OperationAuditAspect.entityFromCommand(new CreateProductCommand(1L))).isEqualTo("Product");
        assertThat(OperationAuditAspect.entityFromCommand(new ApproveCreditPurchaseRequestCommand(1L))).isEqualTo("CreditPurchaseRequest");
        assertThat(OperationAuditAspect.entityFromCommand(new UpdateProductStatusCommand(1L))).isEqualTo("Product");
        assertThat(OperationAuditAspect.entityFromCommand(new UnassignUserFromStoreCommand(1L))).isEqualTo("UserFromStore");
    }

    @Test
    void pullsEntityIdFromTheFirstLongFieldEndingInId() {
        assertThat(OperationAuditAspect.entityIdFromCommand(new UpdateProductCommand(42L))).isEqualTo(42L);
        assertThat(OperationAuditAspect.entityIdFromCommand(new RegisterCreditSaleCommand(7L))).isEqualTo(7L);
        assertThat(OperationAuditAspect.entityIdFromCommand(new CreateProductCommand(1L))).isEqualTo(1L);
        assertThat(OperationAuditAspect.entityIdFromCommand(new SeedRolesCommand())).isNull();
    }

    // The aspect only looks at the class simple name and the record components, so these local
    // records are enough to cover the classification without pulling the real commands into the
    // test (many of them have long constructor validation).
    private record CreateProductCommand(Long productId) {}
    private record UpdateProductCommand(Long productId) {}
    private record UpdateProductStatusCommand(Long productId) {}
    private record DeleteProductCommand(Long productId) {}
    private record RegisterCreditSaleCommand(Long creditProfileId) {}
    private record ApproveCreditPurchaseRequestCommand(Long purchaseRequestId) {}
    private record RejectCreditPurchaseRequestCommand(Long purchaseRequestId) {}
    private record AssignUserToStoreCommand(Long userId) {}
    private record UnassignUserFromStoreCommand(Long userId) {}
    private record UploadProductImageCommand(Long productId) {}
    private record SeedRolesCommand() {}
    private record SimulateCreditSaleCommand(Long creditProfileId) {}
    private record MarkOverdueInstallmentsCommand() {}
    private record GenerateAccountStatementCommand() {}
}
