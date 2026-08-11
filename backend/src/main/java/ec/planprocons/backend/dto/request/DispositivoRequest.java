package ec.planprocons.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DispositivoRequest {

    @NotBlank(message = "El código es obligatorio")
    @Size(max = 50, message = "El código no puede superar 50 caracteres")
    private String codigo;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    private String nombre;

    @Size(max = 100, message = "El modelo no puede superar 100 caracteres")
    private String modelo;

    @NotBlank(message = "El serial es obligatorio")
    @Size(max = 100, message = "El serial no puede superar 100 caracteres")
    private String serial;

    @Size(max = 45, message = "La IP no puede superar 45 caracteres")
    private String ip;

    @Size(max = 150, message = "La ubicación no puede superar 150 caracteres")
    private String ubicacion;

    private LocalDateTime ultimoContacto;
}
