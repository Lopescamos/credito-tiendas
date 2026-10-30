package pe.edu.upc.center.creditotiendas.storemanagement.domain.model.commands;

import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.valueobjects.Ruc;

import static pe.edu.upc.center.creditotiendas.storemanagement.domain.model.commands.StoreManagementCommandRules.*;

/**
 * Registers a store (US-13). Creating a store does not create its store admin; that is US-14.
 */
public record CreateStoreCommand(String ruc, String legalName, String tradeName, String address, String phone) {
    public CreateStoreCommand {
        ruc = new Ruc(ruc).value();
        legalName = requireText(legalName, "legalName", 120);
        tradeName = requireText(tradeName, "tradeName", 100);
        address = requireText(address, "address", 200);
        phone = optionalText(phone, "phone", 15);
    }
}
