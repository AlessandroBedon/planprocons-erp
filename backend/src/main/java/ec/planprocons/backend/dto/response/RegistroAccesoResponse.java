package ec.planprocons.backend.dto.response;

import ec.planprocons.backend.entity.enums.EstadoAcceso;
import ec.planprocons.backend.entity.enums.MetodoVerificacion;
import ec.planprocons.backend.entity.enums.TipoEvento;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RegistroAccesoResponse {

    private Long id;
    private Long personaId;
    private String codigoPersona;
    private String nombrePersona;
    private Long dispositivoId;
    private String codigoDispositivo;
    private String nombreDispositivo;
    private LocalDateTime fechaHora;
    private TipoEvento tipoEvento;
    private MetodoVerificacion metodoVerificacion;
    private EstadoAcceso estado;
    private String codigoEvento;
    private LocalDateTime fechaRecepcion;
}
