package pe.edu.upc.center.creditotiendas.iam.infrastructure.persistence.jpa.adapters;

import org.springframework.stereotype.Repository;
import pe.edu.upc.center.creditotiendas.iam.domain.model.aggregates.Role;
import pe.edu.upc.center.creditotiendas.iam.domain.model.valueobjects.Roles;
import pe.edu.upc.center.creditotiendas.iam.domain.repositories.RoleRepository;
import pe.edu.upc.center.creditotiendas.iam.infrastructure.persistence.jpa.assemblers.RolePersistenceAssembler;
import pe.edu.upc.center.creditotiendas.iam.infrastructure.persistence.jpa.repositories.RolePersistenceRepository;

import java.util.List;

@Repository
public class RoleRepositoryImpl implements RoleRepository {

    private final RolePersistenceRepository rolePersistenceRepository;

    public RoleRepositoryImpl(RolePersistenceRepository rolePersistenceRepository) {
        this.rolePersistenceRepository = rolePersistenceRepository;
    }

    @Override
    public List<Role> findAll() {
        return rolePersistenceRepository.findAll().stream()
                .map(RolePersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public boolean existsByName(Roles name) {
        return rolePersistenceRepository.existsByName(name);
    }

    @Override
    public Role save(Role role) {
        var saved = rolePersistenceRepository.save(RolePersistenceAssembler.toPersistenceFromDomain(role));
        return RolePersistenceAssembler.toDomainFromPersistence(saved);
    }
}
