package ec.planprocons.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ResumenAnomaliasResponse {

    private LocalDate desde;
    private LocalDate hasta;
    private long registrosAnalizados;
    private long totalAnomalias;
    private long accesosNocturnos;
    private long accesosTempranos;
    private long accesosTardios;
    private long accesosRepetitivos;
    private long rechazosRepetitivos;
    private long desviacionesHorario;
}
