package pe.edu.upc.center.creditotiendas.storemanagement.application.commandservices;

import pe.edu.upc.center.creditotiendas.shared.application.result.ApplicationError;
import pe.edu.upc.center.creditotiendas.shared.application.result.Result;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.aggregates.Store;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.commands.CreateStoreCommand;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.commands.UpdateStoreCommand;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.commands.UpdateStoreStatusCommand;

public interface StoreCommandService {
    Result<Long, ApplicationError> handle(CreateStoreCommand command);
    Result<Store, ApplicationError> handle(UpdateStoreCommand command);
    Result<Store, ApplicationError> handle(UpdateStoreStatusCommand command);
}
