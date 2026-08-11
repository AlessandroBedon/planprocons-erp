package ec.planprocons.backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardResumenResponse {

    private long totalAccesos;
    private long entradas;
    private long salidas;
    private long permitidos;
    private long rechazados;
    private long personasUnicas;
    private Integer horaPico;
    private long cantidadHoraPico;
}
