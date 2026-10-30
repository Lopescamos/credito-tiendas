package pe.edu.upc.center.creditotiendas.storemanagement.infrastructure.persistence.jpa.assemblers;

import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.aggregates.Product;
import pe.edu.upc.center.creditotiendas.storemanagement.infrastructure.persistence.jpa.entities.ProductPersistenceEntity;

public final class ProductPersistenceAssembler {

    private ProductPersistenceAssembler() {
    }

    public static Product toDomainFromPersistence(ProductPersistenceEntity entity) {
        if (entity == null) return null;
        var product = new Product();
        product.setId(entity.getId());
        product.setStoreId(entity.getStoreId());
        product.setName(entity.getName());
        product.setDescription(entity.getDescription());
        product.setType(entity.getType());
        product.setSupplier(entity.getSupplier());
        product.setBrand(entity.getBrand());
        product.setMeasurementUnitId(entity.getMeasurementUnitId());
        product.setImageUrl(entity.getImageUrl());
        product.setCashPrice(entity.getCashPrice());
        product.setCreditPrice(entity.getCreditPrice());
        product.setActive(entity.isActive());
        return product;
    }

    public static ProductPersistenceEntity toPersistenceFromDomain(Product product) {
        if (product == null) return null;
        var entity = new ProductPersistenceEntity();
        if (product.getId() != null) entity.setId(product.getId());
        entity.setStoreId(product.getStoreId());
        entity.setName(product.getName());
        entity.setDescription(product.getDescription());
        entity.setType(product.getType());
        entity.setSupplier(product.getSupplier());
        entity.setBrand(product.getBrand());
        entity.setMeasurementUnitId(product.getMeasurementUnitId());
        entity.setImageUrl(product.getImageUrl());
        entity.setCashPrice(product.getCashPrice());
        entity.setCreditPrice(product.getCreditPrice());
        entity.setActive(product.isActive());
        return entity;
    }
}
