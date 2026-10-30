package pe.edu.upc.center.creditotiendas.shared.application.result;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationErrorTest {

    @Test
    void notFoundBuildsUpperSnakeCodeFromCamelCaseAndKeepsIdentifierInDetails() {
        var error = ApplicationError.notFound("CreditSale", "15");

        assertThat(error.code()).isEqualTo("CREDIT_SALE_NOT_FOUND");
        assertThat(error.details()).isEqualTo("15");
    }

    @Test
    void conflictAcceptsKebabCaseResourceNames() {
        var error = ApplicationError.conflict("credit-profile", "already exists");

        assertThat(error.code()).isEqualTo("CREDIT_PROFILE_CONFLICT");
    }

    @Test
    void forbiddenAndUnauthorizedUseFixedCodes() {
        assertThat(ApplicationError.forbidden("Store", "not linked").code()).isEqualTo("FORBIDDEN");
        assertThat(ApplicationError.unauthorized("missing token").code()).isEqualTo("UNAUTHORIZED");
    }
}
