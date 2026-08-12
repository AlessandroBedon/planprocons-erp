package ec.planprocons.backend.repository.projection;

import java.time.LocalDateTime;

public interface AnomaliaProjection {

    String getTipo();

    Long getPersonaId();

    String getCodigoPersona();

    String getNombres();

    String getApellidos();

    Long getRegistroAccesoId();

    LocalDateTime getFechaHora();

    Long getTotalCount();
}
