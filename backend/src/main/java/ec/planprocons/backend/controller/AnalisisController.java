package ec.planprocons.backend.controller;

import ec.planprocons.backend.analysis.enums.TipoAnomalia;
import ec.planprocons.backend.dto.response.AnomaliaResponse;
import ec.planprocons.backend.dto.response.ApiResponse;
import ec.planprocons.backend.dto.response.PatronGeneralResponse;
import ec.planprocons.backend.dto.response.PatronPersonaResponse;
import ec.planprocons.backend.dto.response.ResumenAnomaliasResponse;
import ec.planprocons.backend.service.interfaces.AnalisisService;
import ec.planprocons.backend.util.ResponseFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/analisis")
@RequiredArgsConstructor
public class AnalisisController {

    private final AnalisisService service;

    @GetMapping("/patrones")
    public ResponseEntity<ApiResponse<PatronGeneralResponse>> obtenerPatrones(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta
    ) {
        return ResponseFactory.ok(
                "Patrones generales obtenidos correctamente",
                service.obtenerPatrones(desde, hasta)
        );
    }

    @GetMapping("/personas/{personaId}/patron")
    public ResponseEntity<ApiResponse<PatronPersonaResponse>> obtenerPatronPersona(
            @PathVariable Long personaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta
    ) {
        return ResponseFactory.ok(
                "Patrón de persona obtenido correctamente",
                service.obtenerPatronPersona(personaId, desde, hasta)
        );
    }

    @GetMapping("/anomalias")
    public ResponseEntity<ApiResponse<Page<AnomaliaResponse>>> obtenerAnomalias(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) TipoAnomalia tipo,
            @RequestParam(required = false) Long personaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseFactory.ok(
                "Anomalías obtenidas correctamente",
                service.obtenerAnomalias(desde, hasta, tipo, personaId, page, size)
        );
    }

    @GetMapping("/anomalias/resumen")
    public ResponseEntity<ApiResponse<ResumenAnomaliasResponse>> obtenerResumenAnomalias(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta
    ) {
        return ResponseFactory.ok(
                "Resumen de anomalías obtenido correctamente",
                service.obtenerResumenAnomalias(desde, hasta)
        );
    }
}
