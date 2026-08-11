package ec.planprocons.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DispositivoResponse {

    private Long id;
    private String codigo;
    private String nombre;
    private String modelo;
    private String serial;
    private String ip;
    private String ubicacion;
    private LocalDateTime ultimoContacto;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
