package pe.edu.upc.center.creditotiendas.storemanagement.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.center.creditotiendas.storemanagement.infrastructure.persistence.jpa.entities.ProductPersistenceEntity;

import java.util.List;

public interface ProductPersistenceRepository extends JpaRepository<ProductPersistenceEntity, Long> {
    List<ProductPersistenceEntity> findAllByStoreIdOrderByNameAsc(Long storeId);
    List<ProductPersistenceEntity> findAllByStoreIdAndActiveTrueOrderByNameAsc(Long storeId);
}
