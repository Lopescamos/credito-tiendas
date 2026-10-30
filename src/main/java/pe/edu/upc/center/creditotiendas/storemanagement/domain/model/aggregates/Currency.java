package pe.edu.upc.center.creditotiendas.storemanagement.domain.model.aggregates;

import lombok.Getter;
import lombok.Setter;
import pe.edu.upc.center.creditotiendas.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

/**
 * Currency in which a customer's credit conditions are agreed (table {@code currencies},
 * model: Moneda). Seeded at startup; there is no API to create or edit currencies.
 */
@Getter
public class Currency extends AbstractDomainAggregateRoot<Currency> {

    @Setter private Long id;
    @Setter private String code;
    @Setter private String name;
    @Setter private String symbol;

    public Currency() {
        this.code = "";
        this.name = "";
        this.symbol = "";
    }

    public Currency(String code, String name, String symbol) {
        this.code = code;
        this.name = name;
        this.symbol = symbol;
    }
}
