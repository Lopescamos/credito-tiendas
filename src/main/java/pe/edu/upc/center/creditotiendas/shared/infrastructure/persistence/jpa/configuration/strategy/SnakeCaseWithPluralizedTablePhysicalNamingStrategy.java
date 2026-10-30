package pe.edu.upc.center.creditotiendas.shared.infrastructure.persistence.jpa.configuration.strategy;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

import java.util.Locale;

import static io.github.encryptorcode.pluralize.Pluralize.pluralize;

/**
 * Snake Case With Pluralized Table Physical Naming Strategy
 * @summary
 * PhysicalNamingStrategy implementation that converts column names to snake_case and table names
 * to pluralized snake_case. The {@code PersistenceEntity} suffix is dropped from table names, so
 * {@code CreditSalePersistenceEntity} maps to {@code credit_sales} without an explicit {@code @Table}.
 *
 * <p>Registered in {@code application.properties} via
 * {@code spring.jpa.hibernate.naming.physical-strategy}.</p>
 *
 * @since 1.0.0
 */
public class SnakeCaseWithPluralizedTablePhysicalNamingStrategy implements PhysicalNamingStrategy {

    private static final String PERSISTENCE_ENTITY_SUFFIX = "PersistenceEntity";

    @Override
    public Identifier toPhysicalCatalogName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
        return null;
    }

    @Override
    public Identifier toPhysicalSchemaName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
        return this.toSnakeCase(identifier);
    }

    @Override
    public Identifier toPhysicalTableName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
        return this.toSnakeCase(this.toPlural(this.withoutPersistenceEntitySuffix(identifier)));
    }

    @Override
    public Identifier toPhysicalSequenceName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
        return this.toSnakeCase(identifier);
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
        return this.toSnakeCase(identifier);
    }

    /**
     * Convert identifier to snake case
     * @param identifier Identifier
     * @return Identifier
     */
    private Identifier toSnakeCase(final Identifier identifier) {
        if (identifier == null) {
            return null;
        }
        final String regex = "([a-z0-9])([A-Z])";
        final String replacement = "$1_$2";
        final String newName = identifier.getText()
                .replaceAll(regex, replacement)
                .toLowerCase(Locale.ROOT);
        return Identifier.toIdentifier(newName, identifier.isQuoted());
    }

    /**
     * Convert identifier to plural
     * @param identifier Identifier
     * @return Identifier
     */
    private Identifier toPlural(final Identifier identifier) {
        if (identifier == null) {
            return null;
        }
        final String newName = pluralize(identifier.getText());
        return Identifier.toIdentifier(newName, identifier.isQuoted());
    }

    /**
     * Drop the {@code PersistenceEntity} suffix that every JPA entity class carries by convention
     * @param identifier Identifier
     * @return Identifier
     */
    private Identifier withoutPersistenceEntitySuffix(final Identifier identifier) {
        if (identifier == null) {
            return null;
        }
        final String text = identifier.getText();
        if (!text.endsWith(PERSISTENCE_ENTITY_SUFFIX) || text.equals(PERSISTENCE_ENTITY_SUFFIX)) {
            return identifier;
        }
        final String newName = text.substring(0, text.length() - PERSISTENCE_ENTITY_SUFFIX.length());
        return Identifier.toIdentifier(newName, identifier.isQuoted());
    }
}
