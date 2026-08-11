package ec.planprocons.backend.repository;

import ec.planprocons.backend.entity.Persona;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PersonaRepository extends JpaRepository<Persona, Long> {

    Optional<Persona> findByCodigoBiometrico(String codigoBiometrico);

    boolean existsByCodigoBiometrico(String codigoBiometrico);

    boolean existsByCedula(String cedula);
}
