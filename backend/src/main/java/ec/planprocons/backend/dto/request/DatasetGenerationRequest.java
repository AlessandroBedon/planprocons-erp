package ec.planprocons.backend.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DatasetGenerationRequest {

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDate fechaFin;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor que cero")
    @Max(value = 500000, message = "La cantidad máxima permitida es 500000")
    private Integer cantidad;

    @NotNull(message = "La seed es obligatoria")
    private Long seed;

    @AssertTrue(message = "Debe confirmar explícitamente la generación del dataset")
    private boolean confirmar;
}
