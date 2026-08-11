package ec.planprocons.backend.dto.request;

import ec.planprocons.backend.entity.enums.EstadoAcceso;
import ec.planprocons.backend.entity.enums.MetodoVerificacion;
import ec.planprocons.backend.entity.enums.TipoEvento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RegistroAccesoRequest {

    @NotBlank(message = "El código de la persona es obligatorio")
    @Size(max = 50, message = "El código de la persona no puede superar 50 caracteres")
    private String codigoPersona;

    @NotBlank(message = "El código del dispositivo es obligatorio")
    @Size(max = 50, message = "El código del dispositivo no puede superar 50 caracteres")
    private String codigoDispositivo;

    @NotNull(message = "La fecha y hora del evento es obligatoria")
    private LocalDateTime fechaHora;

    @NotNull(message = "El tipo de evento es obligatorio")
    private TipoEvento tipoEvento;

    @NotNull(message = "El método de verificación es obligatorio")
    private MetodoVerificacion metodoVerificacion;

    @NotNull(message = "El estado del acceso es obligatorio")
    private EstadoAcceso estado;

    @Size(max = 100, message = "El código del evento no puede superar 100 caracteres")
    private String codigoEvento;
}
