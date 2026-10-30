package pe.edu.upc.center.creditotiendas.storemanagement.infrastructure.persistence.jpa.assemblers;

import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.aggregates.Store;
import pe.edu.upc.center.creditotiendas.storemanagement.infrastructure.persistence.jpa.entities.StorePersistenceEntity;

public final class StorePersistenceAssembler {

    private StorePersistenceAssembler() {
    }

    public static Store toDomainFromPersistence(StorePersistenceEntity entity) {
        if (entity == null) return null;
        var store = new Store();
        store.setId(entity.getId());
        store.setRuc(entity.getRuc());
        store.setLegalName(entity.getLegalName());
        store.setTradeName(entity.getTradeName());
        store.setAddress(entity.getAddress());
        store.setPhone(entity.getPhone());
        store.setActive(entity.isActive());
        return store;
    }

    public static StorePersistenceEntity toPersistenceFromDomain(Store store) {
        if (store == null) return null;
        var entity = new StorePersistenceEntity();
        if (store.getId() != null) entity.setId(store.getId());
        entity.setRuc(store.getRuc());
        entity.setLegalName(store.getLegalName());
        entity.setTradeName(store.getTradeName());
        entity.setAddress(store.getAddress());
        entity.setPhone(store.getPhone());
        entity.setActive(store.isActive());
        return entity;
    }
}
