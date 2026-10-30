package pe.edu.upc.center.creditotiendas.iam.domain.repositories;

import pe.edu.upc.center.creditotiendas.iam.domain.model.aggregates.User;
import pe.edu.upc.center.creditotiendas.iam.domain.model.valueobjects.EmailAddress;
import pe.edu.upc.center.creditotiendas.iam.domain.model.valueobjects.Roles;

import java.util.List;
import java.util.Optional;

/**
 * Persistence port of the {@link User} aggregate, including its store assignments.
 */
public interface UserRepository {
    Optional<User> findById(Long id);
    Optional<User> findByEmail(EmailAddress email);
    List<User> findAll();
    boolean existsByEmail(EmailAddress email);
    boolean existsByRole(Roles role);
    User save(User user);
}
