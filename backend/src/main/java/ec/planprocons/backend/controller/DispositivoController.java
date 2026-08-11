package ec.planprocons.backend.controller;

import ec.planprocons.backend.dto.request.DispositivoRequest;
import ec.planprocons.backend.dto.response.ApiResponse;
import ec.planprocons.backend.dto.response.DispositivoResponse;
import ec.planprocons.backend.service.interfaces.DispositivoService;
import ec.planprocons.backend.util.ResponseFactory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dispositivos")
@RequiredArgsConstructor
public class DispositivoController {

    private final DispositivoService service;

    @PostMapping
    public ResponseEntity<ApiResponse<DispositivoResponse>> guardar(
            @Valid @RequestBody DispositivoRequest request
    ) {

        return ResponseFactory.created(
                "Dispositivo creado correctamente",
                service.guardar(request)
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DispositivoResponse>>> listar() {

        return ResponseFactory.ok(
                "Dispositivos obtenidos correctamente",
                service.listar()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DispositivoResponse>> obtenerPorId(
            @PathVariable Long id
    ) {

        return ResponseFactory.ok(
                "Dispositivo obtenido correctamente",
                service.obtenerPorId(id)
        );
    }

    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<ApiResponse<DispositivoResponse>> obtenerPorCodigo(
            @PathVariable String codigo
    ) {

        return ResponseFactory.ok(
                "Dispositivo obtenido correctamente",
                service.obtenerPorCodigo(codigo)
        );
    }
}
