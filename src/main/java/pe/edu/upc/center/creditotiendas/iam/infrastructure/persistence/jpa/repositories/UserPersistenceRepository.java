package pe.edu.upc.center.creditotiendas.iam.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.center.creditotiendas.iam.domain.model.valueobjects.EmailAddress;
import pe.edu.upc.center.creditotiendas.iam.domain.model.valueobjects.Roles;
import pe.edu.upc.center.creditotiendas.iam.infrastructure.persistence.jpa.entities.UserPersistenceEntity;

import java.util.Optional;

public interface UserPersistenceRepository extends JpaRepository<UserPersistenceEntity, Long> {
    Optional<UserPersistenceEntity> findByEmail(EmailAddress email);
    boolean existsByEmail(EmailAddress email);
    boolean existsByRole_Name(Roles roleName);
}
