package pe.edu.upc.center.creditotiendas.storemanagement.application.internal.commandservices;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.upc.center.creditotiendas.shared.application.result.ApplicationError;
import pe.edu.upc.center.creditotiendas.shared.application.result.Result;
import pe.edu.upc.center.creditotiendas.storemanagement.application.commandservices.StoreCommandService;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.aggregates.Store;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.commands.CreateStoreCommand;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.commands.UpdateStoreCommand;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.commands.UpdateStoreStatusCommand;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.valueobjects.Ruc;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.repositories.StoreRepository;

import java.util.function.Function;

/**
 * Store administration (US-13).
 */
@Slf4j
@Service
public class StoreCommandServiceImpl implements StoreCommandService {

    private final StoreRepository storeRepository;

    public StoreCommandServiceImpl(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    /**
     * Rejects a RUC that is already registered (US-13: unique RUC).
     */
    @Override
    public Result<Long, ApplicationError> handle(CreateStoreCommand command) {
        var ruc = new Ruc(command.ruc());
        if (storeRepository.existsByRuc(ruc))
            return Result.failure(ApplicationError.conflict(
                    "Store", "A store with RUC '%s' already exists".formatted(ruc.value())));
        try {
            return Result.success(storeRepository.save(new Store(command)).getId());
        } catch (Exception e) {
            log.error("Error in create-store", e);
            return Result.failure(ApplicationError.unexpected("create-store", "The store could not be created"));
        }
    }

    @Override
    public Result<Store, ApplicationError> handle(UpdateStoreCommand command) {
        return updateStore(command.storeId(), "update-store", store -> store.updateInformation(
                command.legalName(), command.tradeName(), command.address(), command.phone()));
    }

    @Override
    public Result<Store, ApplicationError> handle(UpdateStoreStatusCommand command) {
        return updateStore(command.storeId(), "update-store-status",
                store -> command.active() ? store.activate() : store.deactivate());
    }

    private Result<Store, ApplicationError> updateStore(Long storeId, String context, Function<Store, Store> change) {
        var store = storeRepository.findById(storeId);
        if (store.isEmpty())
            return Result.failure(ApplicationError.notFound("Store", storeId.toString()));
        try {
            return Result.success(storeRepository.save(change.apply(store.get())));
        } catch (Exception e) {
            log.error("Error in {}", context, e);
            return Result.failure(ApplicationError.unexpected(context, "The store could not be updated"));
        }
    }
}
