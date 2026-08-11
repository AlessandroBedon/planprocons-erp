package ec.planprocons.backend.dto.response;

import ec.planprocons.backend.analysis.enums.NivelAnomalia;
import ec.planprocons.backend.analysis.enums.TipoAnomalia;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AnomaliaResponse {

    private TipoAnomalia tipo;
    private NivelAnomalia nivel;
    private Long personaId;
    private String codigoPersona;
    private String nombrePersona;
    private Long registroAccesoId;
    private LocalDateTime fechaHora;
    private String descripcion;
}
