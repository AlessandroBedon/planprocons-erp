package ec.planprocons.backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DatasetGenerationResponse {

    private String lote;
    private int solicitados;
    private int insertados;
    private int personasDisponibles;
    private int dispositivosDisponibles;
    private long tiempoMs;
    private double registrosPorSegundo;
}
