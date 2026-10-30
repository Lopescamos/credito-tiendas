package pe.edu.upc.center.creditotiendas.storemanagement.domain.model.aggregates;

import lombok.Getter;
import lombok.Setter;
import pe.edu.upc.center.creditotiendas.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.commands.CreateStoreCommand;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.valueobjects.Ruc;

/**
 * A business (tenant) that sells on credit to its customers (US-13).
 * A deactivated store cannot receive new credit sales nor newly linked users.
 */
@Getter
public class Store extends AbstractDomainAggregateRoot<Store> {

    @Setter private Long id;
    @Setter private Ruc ruc;
    @Setter private String legalName;
    @Setter private String tradeName;
    @Setter private String address;
    @Setter private String phone;
    @Setter private boolean active;

    public Store() {
        this.legalName = "";
        this.tradeName = "";
        this.address = "";
        this.active = true;
    }

    public Store(CreateStoreCommand command) {
        this();
        this.ruc = new Ruc(command.ruc());
        this.legalName = command.legalName();
        this.tradeName = command.tradeName();
        this.address = command.address();
        this.phone = command.phone();
    }

    public Store updateInformation(String legalName, String tradeName, String address, String phone) {
        this.legalName = legalName;
        this.tradeName = tradeName;
        this.address = address;
        this.phone = phone;
        return this;
    }

    public Store activate() {
        this.active = true;
        return this;
    }

    public Store deactivate() {
        this.active = false;
        return this;
    }
}
