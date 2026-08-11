package ec.planprocons.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PersonaRequest {

    @NotBlank(message = "El código biométrico es obligatorio")
    @Size(max = 50, message = "El código biométrico no puede superar 50 caracteres")
    private String codigoBiometrico;

    @NotBlank(message = "La cédula es obligatoria")
    @Pattern(regexp = "\\d{10}", message = "La cédula debe contener exactamente 10 dígitos")
    private String cedula;

    @NotBlank(message = "Los nombres son obligatorios")
    @Size(max = 100, message = "Los nombres no pueden superar 100 caracteres")
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 100, message = "Los apellidos no pueden superar 100 caracteres")
    private String apellidos;

    @Size(max = 100, message = "El departamento no puede superar 100 caracteres")
    private String departamento;

    @Size(max = 100, message = "El cargo no puede superar 100 caracteres")
    private String cargo;
}
