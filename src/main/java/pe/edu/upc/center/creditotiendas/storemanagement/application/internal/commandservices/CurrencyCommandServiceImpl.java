package pe.edu.upc.center.creditotiendas.storemanagement.application.internal.commandservices;

import org.springframework.stereotype.Service;
import pe.edu.upc.center.creditotiendas.storemanagement.application.commandservices.CurrencyCommandService;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.aggregates.Currency;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.commands.SeedCurrenciesCommand;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.repositories.CurrencyRepository;

import java.util.List;

@Service
public class CurrencyCommandServiceImpl implements CurrencyCommandService {

    private static final List<Currency> DEFAULT_CURRENCIES = List.of(
            new Currency("PEN", "Sol peruano", "S/"),
            new Currency("USD", "Dólar estadounidense", "US$"));

    private final CurrencyRepository currencyRepository;

    public CurrencyCommandServiceImpl(CurrencyRepository currencyRepository) {
        this.currencyRepository = currencyRepository;
    }

    /**
     * Inserts only the missing currencies, matched by ISO code.
     */
    @Override
    public void handle(SeedCurrenciesCommand command) {
        DEFAULT_CURRENCIES.stream()
                .filter(currency -> !currencyRepository.existsByCode(currency.getCode()))
                .forEach(currency -> currencyRepository.save(
                        new Currency(currency.getCode(), currency.getName(), currency.getSymbol())));
    }
}
