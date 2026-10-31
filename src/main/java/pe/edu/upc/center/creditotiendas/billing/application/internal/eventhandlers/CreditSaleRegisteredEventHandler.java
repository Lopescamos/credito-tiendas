package pe.edu.upc.center.creditotiendas.billing.application.internal.eventhandlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import pe.edu.upc.center.creditotiendas.billing.application.commandservices.InstallmentCommandService;
import pe.edu.upc.center.creditotiendas.billing.domain.model.commands.CreateInstallmentScheduleCommand;
import pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.events.CreditSaleRegisteredIntegrationEvent;

/**
 * Listens to Credit's {@link CreditSaleRegisteredIntegrationEvent} and creates the installments of the sale.
 * Runs inside Credit's transaction (synchronous {@code @EventListener}), so a failure here rolls the sale
 * back and the customer sees the error straight away.
 */
@Slf4j
@Service("billingCreditSaleRegisteredEventHandler")
public class CreditSaleRegisteredEventHandler {

    private final InstallmentCommandService installmentCommandService;

    public CreditSaleRegisteredEventHandler(InstallmentCommandService installmentCommandService) {
        this.installmentCommandService = installmentCommandService;
    }

    @EventListener
    public void on(CreditSaleRegisteredIntegrationEvent event) {
        installmentCommandService.handle(new CreateInstallmentScheduleCommand(
                event.creditSaleId(), event.creditProfileId(), event.installments()));
    }
}
