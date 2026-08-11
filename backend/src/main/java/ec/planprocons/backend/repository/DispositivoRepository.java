package ec.planprocons.backend.repository;

import ec.planprocons.backend.entity.Dispositivo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DispositivoRepository extends JpaRepository<Dispositivo, Long> {

    Optional<Dispositivo> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    boolean existsBySerial(String serial);
}
