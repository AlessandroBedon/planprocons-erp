package ec.planprocons.backend.repository;

import ec.planprocons.backend.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository <Role, Long> {

    Optional<Role>
    findByNombre(String nombre);

    boolean existsByNombre(String nombre);

}
