package ec.planprocons.backend.controller;

import ec.planprocons.backend.dto.request.PersonaRequest;
import ec.planprocons.backend.dto.response.ApiResponse;
import ec.planprocons.backend.dto.response.PersonaResponse;
import ec.planprocons.backend.service.interfaces.PersonaService;
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
@RequestMapping("/api/personas")
@RequiredArgsConstructor
public class PersonaController {

    private final PersonaService service;

    @PostMapping
    public ResponseEntity<ApiResponse<PersonaResponse>> guardar(
            @Valid @RequestBody PersonaRequest request
    ) {

        return ResponseFactory.created(
                "Persona creada correctamente",
                service.guardar(request)
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PersonaResponse>>> listar() {

        return ResponseFactory.ok(
                "Personas obtenidas correctamente",
                service.listar()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PersonaResponse>> obtenerPorId(
            @PathVariable Long id
    ) {

        return ResponseFactory.ok(
                "Persona obtenida correctamente",
                service.obtenerPorId(id)
        );
    }

    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<ApiResponse<PersonaResponse>> obtenerPorCodigo(
            @PathVariable String codigo
    ) {

        return ResponseFactory.ok(
                "Persona obtenida correctamente",
                service.obtenerPorCodigo(codigo)
        );
    }
}
