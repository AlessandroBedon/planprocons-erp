package ec.planprocons.backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PersonaFrecuenteResponse {

    private Long personaId;
    private String codigoBiometrico;
    private String nombre;
    private long cantidadAccesos;
}
