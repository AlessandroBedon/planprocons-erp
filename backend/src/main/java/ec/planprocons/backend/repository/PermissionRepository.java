package ec.planprocons.backend.repository;

import ec.planprocons.backend.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByNombre(String nombre);
}
