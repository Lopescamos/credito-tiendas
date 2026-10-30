package pe.edu.upc.center.creditotiendas.shared.infrastructure.persistence.jpa.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables Spring Data JPA auditing so {@code @CreatedDate} and {@code @LastModifiedDate}
 * in {@code AuditableAbstractPersistenceEntity} are populated automatically.
 *
 * <p>Kept out of the main application class on purpose: test slices exclude scanned
 * {@code @Configuration} classes, so {@code @WebMvcTest} does not try to start JPA.
 * {@code @DataJpaTest} classes must {@code @Import(JpaAuditingConfiguration.class)}.</p>
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfiguration {
}
