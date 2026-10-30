package pe.edu.upc.center.creditotiendas.storemanagement.infrastructure.persistence.jpa.assemblers;

import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.aggregates.Currency;
import pe.edu.upc.center.creditotiendas.storemanagement.infrastructure.persistence.jpa.entities.CurrencyPersistenceEntity;

public final class CurrencyPersistenceAssembler {

    private CurrencyPersistenceAssembler() {
    }

    public static Currency toDomainFromPersistence(CurrencyPersistenceEntity entity) {
        if (entity == null) return null;
        var currency = new Currency();
        currency.setId(entity.getId());
        currency.setCode(entity.getCode());
        currency.setName(entity.getName());
        currency.setSymbol(entity.getSymbol());
        return currency;
    }

    public static CurrencyPersistenceEntity toPersistenceFromDomain(Currency currency) {
        if (currency == null) return null;
        var entity = new CurrencyPersistenceEntity();
        if (currency.getId() != null) entity.setId(currency.getId());
        entity.setCode(currency.getCode());
        entity.setName(currency.getName());
        entity.setSymbol(currency.getSymbol());
        return entity;
    }
}
