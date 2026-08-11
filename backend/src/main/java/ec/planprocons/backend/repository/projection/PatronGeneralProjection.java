package ec.planprocons.backend.repository.projection;

import java.time.LocalDate;

public interface PatronGeneralProjection {

    Long getRegistrosAnalizados();

    Integer getHoraPicoGeneral();

    Long getCantidadHoraPicoGeneral();

    Integer getHoraPicoEntradas();

    Long getCantidadHoraPicoEntradas();

    Integer getHoraPicoSalidas();

    Long getCantidadHoraPicoSalidas();

    LocalDate getFechaMayorActividad();

    Long getCantidadFechaMayorActividad();

    Integer getDiaSemanaMayorActividad();
}
