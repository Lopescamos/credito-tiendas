package pe.edu.upc.center.creditotiendas.billing.infrastructure.scheduling;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Enables {@code @Scheduled} jobs. Lives in Billing because the only scheduled jobs so far are its own.
 */
@Configuration
@EnableScheduling
public class BillingSchedulingConfiguration {
}
