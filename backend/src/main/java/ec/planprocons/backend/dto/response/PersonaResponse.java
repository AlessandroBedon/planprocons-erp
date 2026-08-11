package ec.planprocons.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PersonaResponse {

    private Long id;
    private String codigoBiometrico;
    private String cedula;
    private String nombres;
    private String apellidos;
    private String departamento;
    private String cargo;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
