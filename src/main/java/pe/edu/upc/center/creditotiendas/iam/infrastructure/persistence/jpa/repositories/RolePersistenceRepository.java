package pe.edu.upc.center.creditotiendas.iam.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.center.creditotiendas.iam.domain.model.valueobjects.Roles;
import pe.edu.upc.center.creditotiendas.iam.infrastructure.persistence.jpa.entities.RolePersistenceEntity;

import java.util.Optional;

public interface RolePersistenceRepository extends JpaRepository<RolePersistenceEntity, Long> {
    Optional<RolePersistenceEntity> findByName(Roles name);
    boolean existsByName(Roles name);
}
