package ec.planprocons.backend.repository.projection;

public interface ResumenAnomaliasProjection {

    Long getRegistrosAnalizados();

    Long getTotalAnomalias();

    Long getAccesosNocturnos();

    Long getAccesosTempranos();

    Long getAccesosTardios();

    Long getAccesosRepetitivos();

    Long getRechazosRepetitivos();

    Long getDesviacionesHorario();
}
