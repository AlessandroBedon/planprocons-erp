package ec.planprocons.backend.controller;

import ec.planprocons.backend.dto.request.RegistroAccesoRequest;
import ec.planprocons.backend.dto.response.ApiResponse;
import ec.planprocons.backend.dto.response.RegistroAccesoResponse;
import ec.planprocons.backend.service.interfaces.RegistroAccesoService;
import ec.planprocons.backend.service.impl.AccessEventStreamService;
import ec.planprocons.backend.util.ResponseFactory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accesos")
@RequiredArgsConstructor
public class RegistroAccesoController {

    private final RegistroAccesoService service;
    private final AccessEventStreamService eventStreamService;

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        return eventStreamService.subscribe();
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RegistroAccesoResponse>> registrar(
            @Valid @RequestBody RegistroAccesoRequest request
    ) {

        return ResponseFactory.created(
                "Registro de acceso creado correctamente",
                service.registrar(request)
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<RegistroAccesoResponse>>> listar(
            @PageableDefault(
                    size = 20,
                    sort = "fechaHora",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {

        return ResponseFactory.ok(
                "Registros de acceso obtenidos correctamente",
                service.listar(pageable)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RegistroAccesoResponse>> obtenerPorId(
            @PathVariable Long id
    ) {

        return ResponseFactory.ok(
                "Registro de acceso obtenido correctamente",
                service.obtenerPorId(id)
        );
    }
}
