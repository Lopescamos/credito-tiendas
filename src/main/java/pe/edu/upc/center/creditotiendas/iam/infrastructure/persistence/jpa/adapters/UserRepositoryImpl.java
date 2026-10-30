package pe.edu.upc.center.creditotiendas.iam.infrastructure.persistence.jpa.adapters;

import org.springframework.stereotype.Repository;
import pe.edu.upc.center.creditotiendas.iam.domain.model.aggregates.User;
import pe.edu.upc.center.creditotiendas.iam.domain.model.valueobjects.EmailAddress;
import pe.edu.upc.center.creditotiendas.iam.domain.model.valueobjects.Roles;
import pe.edu.upc.center.creditotiendas.iam.domain.repositories.UserRepository;
import pe.edu.upc.center.creditotiendas.iam.infrastructure.persistence.jpa.assemblers.UserPersistenceAssembler;
import pe.edu.upc.center.creditotiendas.iam.infrastructure.persistence.jpa.repositories.RolePersistenceRepository;
import pe.edu.upc.center.creditotiendas.iam.infrastructure.persistence.jpa.repositories.UserPersistenceRepository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final UserPersistenceRepository userPersistenceRepository;
    private final RolePersistenceRepository rolePersistenceRepository;

    public UserRepositoryImpl(UserPersistenceRepository userPersistenceRepository,
                              RolePersistenceRepository rolePersistenceRepository) {
        this.userPersistenceRepository = userPersistenceRepository;
        this.rolePersistenceRepository = rolePersistenceRepository;
    }

    @Override
    public Optional<User> findById(Long id) {
        return userPersistenceRepository.findById(id)
                .map(UserPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public Optional<User> findByEmail(EmailAddress email) {
        return userPersistenceRepository.findByEmail(email)
                .map(UserPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<User> findAll() {
        return userPersistenceRepository.findAll().stream()
                .map(UserPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public boolean existsByEmail(EmailAddress email) {
        return userPersistenceRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByRole(Roles role) {
        return userPersistenceRepository.existsByRole_Name(role);
    }

    @Override
    public User save(User user) {
        var role = rolePersistenceRepository.findByName(user.getRole())
                .orElseThrow(() -> new IllegalStateException("Role %s has not been seeded".formatted(user.getRole())));
        var saved = userPersistenceRepository.save(UserPersistenceAssembler.toPersistenceFromDomain(user, role));
        return UserPersistenceAssembler.toDomainFromPersistence(saved);
    }
}
