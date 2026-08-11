package ec.planprocons.backend.service.interfaces;

import ec.planprocons.backend.dto.response.AccesoPorDiaResponse;
import ec.planprocons.backend.dto.response.AccesoPorHoraResponse;
import ec.planprocons.backend.dto.response.DashboardResumenResponse;
import ec.planprocons.backend.dto.response.PersonaFrecuenteResponse;

import java.time.LocalDate;
import java.util.List;

public interface DashboardService {

    DashboardResumenResponse obtenerResumen(LocalDate fecha);

    List<AccesoPorHoraResponse> obtenerAccesosPorHora(LocalDate fecha);

    List<AccesoPorDiaResponse> obtenerAccesosPorDia(LocalDate desde, LocalDate hasta);

    List<PersonaFrecuenteResponse> obtenerPersonasFrecuentes(
            LocalDate desde,
            LocalDate hasta,
            int limite
    );
}
