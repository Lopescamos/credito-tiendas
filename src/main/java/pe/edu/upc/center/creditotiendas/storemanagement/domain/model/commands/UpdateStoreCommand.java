package pe.edu.upc.center.creditotiendas.storemanagement.domain.model.commands;

import static pe.edu.upc.center.creditotiendas.storemanagement.domain.model.commands.StoreManagementCommandRules.*;

/**
 * Updates the contact and business names of a store. The RUC is not editable.
 */
public record UpdateStoreCommand(Long storeId, String legalName, String tradeName, String address, String phone) {
    public UpdateStoreCommand {
        storeId = requireId(storeId, "storeId");
        legalName = requireText(legalName, "legalName", 120);
        tradeName = requireText(tradeName, "tradeName", 100);
        address = requireText(address, "address", 200);
        phone = optionalText(phone, "phone", 15);
    }
}
