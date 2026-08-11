package ec.planprocons.backend.repository;

import ec.planprocons.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByCedula(String cedula);

    Optional<User> findByCorreo(String correo);

}
