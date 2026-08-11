package ec.planprocons.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class PatronGeneralResponse {

    private LocalDate desde;
    private LocalDate hasta;
    private long registrosAnalizados;
    private Integer horaPicoGeneral;
    private long cantidadHoraPicoGeneral;
    private Integer horaPicoEntradas;
    private long cantidadHoraPicoEntradas;
    private Integer horaPicoSalidas;
    private long cantidadHoraPicoSalidas;
    private LocalDate fechaMayorActividad;
    private long cantidadFechaMayorActividad;
    private String diaSemanaMayorActividad;
}
