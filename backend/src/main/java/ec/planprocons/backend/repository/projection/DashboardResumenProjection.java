package ec.planprocons.backend.repository.projection;

public interface DashboardResumenProjection {

    Long getTotalAccesos();

    Long getEntradas();

    Long getSalidas();

    Long getPermitidos();

    Long getRechazados();

    Long getPersonasUnicas();
}
