package ec.planprocons.backend.service.interfaces;

import ec.planprocons.backend.analysis.enums.TipoAnomalia;
import ec.planprocons.backend.dto.response.AnomaliaResponse;
import ec.planprocons.backend.dto.response.PatronGeneralResponse;
import ec.planprocons.backend.dto.response.PatronPersonaResponse;
import ec.planprocons.backend.dto.response.ResumenAnomaliasResponse;
import org.springframework.data.domain.Page;

import java.time.LocalDate;

public interface AnalisisService {

    PatronGeneralResponse obtenerPatrones(LocalDate desde, LocalDate hasta);

    PatronPersonaResponse obtenerPatronPersona(
            Long personaId,
            LocalDate desde,
            LocalDate hasta
    );

    Page<AnomaliaResponse> obtenerAnomalias(
            LocalDate desde,
            LocalDate hasta,
            TipoAnomalia tipo,
            Long personaId,
            int pagina,
            int tamano
    );

    ResumenAnomaliasResponse obtenerResumenAnomalias(
            LocalDate desde,
            LocalDate hasta
    );
}
