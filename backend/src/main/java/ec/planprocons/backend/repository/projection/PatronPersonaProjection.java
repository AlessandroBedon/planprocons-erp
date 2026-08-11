package ec.planprocons.backend.repository.projection;

public interface PatronPersonaProjection {

    Double getMinutoHabitualEntrada();

    Double getMinutoHabitualSalida();

    Double getPromedioAccesosDiarios();

    Long getDiasAnalizados();

    Long getDiasConEntrada();
}
