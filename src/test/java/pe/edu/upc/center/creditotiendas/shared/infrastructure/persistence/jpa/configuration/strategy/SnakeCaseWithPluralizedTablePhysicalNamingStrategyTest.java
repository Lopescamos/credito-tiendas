package pe.edu.upc.center.creditotiendas.shared.infrastructure.persistence.jpa.configuration.strategy;

import org.hibernate.boot.model.naming.Identifier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class SnakeCaseWithPluralizedTablePhysicalNamingStrategyTest {

    private final SnakeCaseWithPluralizedTablePhysicalNamingStrategy strategy =
            new SnakeCaseWithPluralizedTablePhysicalNamingStrategy();

    @ParameterizedTest
    @CsvSource({
            "CreditSalePersistenceEntity, credit_sales",
            "CurrencyPersistenceEntity, currencies",
            "StorePersistenceEntity, stores",
            "SaleDetailPersistenceEntity, sale_details",
            "products, products",
            "CreditSale, credit_sales"
    })
    void tableNamesArePluralSnakeCaseWithoutPersistenceEntitySuffix(String entityName, String expected) {
        var result = strategy.toPhysicalTableName(Identifier.toIdentifier(entityName), null);

        assertThat(result.getText()).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "createdAt, created_at",
            "interestRateSnapshot, interest_rate_snapshot",
            "id, id",
            "address2Line, address2_line"
    })
    void columnNamesAreSnakeCase(String fieldName, String expected) {
        var result = strategy.toPhysicalColumnName(Identifier.toIdentifier(fieldName), null);

        assertThat(result.getText()).isEqualTo(expected);
    }
}
