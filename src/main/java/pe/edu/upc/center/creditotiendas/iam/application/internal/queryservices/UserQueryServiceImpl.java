package pe.edu.upc.center.creditotiendas.iam.application.internal.queryservices;

import org.springframework.stereotype.Service;
import pe.edu.upc.center.creditotiendas.iam.application.queryservices.UserQueryService;
import pe.edu.upc.center.creditotiendas.iam.domain.model.aggregates.User;
import pe.edu.upc.center.creditotiendas.iam.domain.model.queries.GetAllUsersQuery;
import pe.edu.upc.center.creditotiendas.iam.domain.model.queries.GetUserByEmailQuery;
import pe.edu.upc.center.creditotiendas.iam.domain.model.queries.GetUserByIdQuery;
import pe.edu.upc.center.creditotiendas.iam.domain.repositories.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UserQueryServiceImpl implements UserQueryService {

    private final UserRepository userRepository;

    public UserQueryServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<User> handle(GetUserByIdQuery query) {
        return userRepository.findById(query.userId());
    }

    @Override
    public Optional<User> handle(GetUserByEmailQuery query) {
        return userRepository.findByEmail(query.email());
    }

    @Override
    public List<User> handle(GetAllUsersQuery query) {
        return userRepository.findAll();
    }
}
