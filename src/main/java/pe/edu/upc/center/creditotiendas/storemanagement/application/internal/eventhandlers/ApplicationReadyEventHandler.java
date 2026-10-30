package pe.edu.upc.center.creditotiendas.storemanagement.application.internal.eventhandlers;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import pe.edu.upc.center.creditotiendas.storemanagement.application.commandservices.CurrencyCommandService;
import pe.edu.upc.center.creditotiendas.storemanagement.application.commandservices.MeasurementUnitCommandService;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.commands.SeedCurrenciesCommand;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.commands.SeedMeasurementUnitsCommand;

/**
 * Seeds the Store Management catalogs (currencies and measurement units) at startup.
 */
@Service("storeManagementApplicationReadyEventHandler")
public class ApplicationReadyEventHandler {

    private final CurrencyCommandService currencyCommandService;
    private final MeasurementUnitCommandService measurementUnitCommandService;

    public ApplicationReadyEventHandler(CurrencyCommandService currencyCommandService,
                                        MeasurementUnitCommandService measurementUnitCommandService) {
        this.currencyCommandService = currencyCommandService;
        this.measurementUnitCommandService = measurementUnitCommandService;
    }

    @EventListener
    public void on(ApplicationReadyEvent event) {
        currencyCommandService.handle(new SeedCurrenciesCommand());
        measurementUnitCommandService.handle(new SeedMeasurementUnitsCommand());
    }
}
