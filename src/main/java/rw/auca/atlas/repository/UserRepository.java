package rw.auca.atlas.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import rw.auca.atlas.model.User;

// REPOSITORY PATTERN: data access abstraction for User
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByUsername(String username);
}
