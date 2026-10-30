package pe.edu.upc.center.creditotiendas.storemanagement.interfaces.rest.transform;

import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.aggregates.Currency;
import pe.edu.upc.center.creditotiendas.storemanagement.interfaces.rest.resources.CurrencyResource;

public class CurrencyResourceFromEntityAssembler {
    public static CurrencyResource toResourceFromEntity(Currency currency) {
        return new CurrencyResource(currency.getId(), currency.getCode(), currency.getName(), currency.getSymbol());
    }
}
