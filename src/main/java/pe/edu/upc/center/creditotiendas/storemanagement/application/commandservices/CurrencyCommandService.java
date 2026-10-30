package pe.edu.upc.center.creditotiendas.storemanagement.application.commandservices;

import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.commands.SeedCurrenciesCommand;

public interface CurrencyCommandService {
    void handle(SeedCurrenciesCommand command);
}
