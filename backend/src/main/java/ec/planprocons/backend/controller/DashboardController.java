package ec.planprocons.backend.controller;

import ec.planprocons.backend.dto.response.AccesoPorDiaResponse;
import ec.planprocons.backend.dto.response.AccesoPorHoraResponse;
import ec.planprocons.backend.dto.response.ApiResponse;
import ec.planprocons.backend.dto.response.DashboardResumenResponse;
import ec.planprocons.backend.dto.response.PersonaFrecuenteResponse;
import ec.planprocons.backend.service.interfaces.DashboardService;
import ec.planprocons.backend.util.ResponseFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService service;

    @GetMapping("/resumen")
    public ResponseEntity<ApiResponse<DashboardResumenResponse>> obtenerResumen(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha
    ) {

        return ResponseFactory.ok(
                "Resumen del dashboard obtenido correctamente",
                service.obtenerResumen(fecha)
        );
    }

    @GetMapping("/accesos-por-hora")
    public ResponseEntity<ApiResponse<List<AccesoPorHoraResponse>>> obtenerAccesosPorHora(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha
    ) {

        return ResponseFactory.ok(
                "Accesos por hora obtenidos correctamente",
                service.obtenerAccesosPorHora(fecha)
        );
    }

    @GetMapping("/accesos-por-dia")
    public ResponseEntity<ApiResponse<List<AccesoPorDiaResponse>>> obtenerAccesosPorDia(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta
    ) {

        return ResponseFactory.ok(
                "Accesos por día obtenidos correctamente",
                service.obtenerAccesosPorDia(desde, hasta)
        );
    }

    @GetMapping("/personas-frecuentes")
    public ResponseEntity<ApiResponse<List<PersonaFrecuenteResponse>>> obtenerPersonasFrecuentes(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "10") int limit
    ) {

        return ResponseFactory.ok(
                "Personas frecuentes obtenidas correctamente",
                service.obtenerPersonasFrecuentes(desde, hasta, limit)
        );
    }
}
