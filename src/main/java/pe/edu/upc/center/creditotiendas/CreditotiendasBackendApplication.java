package pe.edu.upc.center.creditotiendas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the CreditoTiendas backend.
 *
 * <p>JPA auditing is enabled in
 * {@code shared.infrastructure.persistence.jpa.configuration.JpaAuditingConfiguration}
 * instead of here, so that test slices such as {@code @WebMvcTest} do not try to
 * bootstrap JPA.</p>
 */
@SpringBootApplication
public class CreditotiendasBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(CreditotiendasBackendApplication.class, args);
	}

}
